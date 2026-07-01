import os
import logging
import joblib
from fastapi import FastAPI
from pydantic import BaseModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("nlp-classifier")

app = FastAPI(title="TicketSense NLP Classifier")

CONFIDENCE_THRESHOLD = float(os.getenv("CONFIDENCE_THRESHOLD", "0.70"))

model = None

class ClassifyRequest(BaseModel):
    summary: str
    description: str = ""

class ClassifyResponse(BaseModel):
    queue: str
    confidence: float

@app.on_event("startup")
def load_model():
    global model
    model_path = os.getenv("MODEL_PATH", "model.joblib")
    if not os.path.exists(model_path):
        from train import train
        train()
    model = joblib.load(model_path)

@app.post("/classify", response_model=ClassifyResponse)
def classify(request: ClassifyRequest):
    text = f"{request.summary} {request.description}".strip()
    probabilities = model.predict_proba([text])[0]
    classes = model.classes_

    logger.info("--- Classification Request ---")
    logger.info(f"  Text: \"{text[:100]}{'...' if len(text) > 100 else ''}\"")
    for cls, prob in sorted(zip(classes, probabilities), key=lambda x: x[1], reverse=True):
        logger.info(f"  {cls}: {prob:.2%}")

    max_idx = probabilities.argmax()
    confidence = float(probabilities[max_idx])
    predicted_queue = classes[max_idx]

    if confidence < CONFIDENCE_THRESHOLD:
        logger.info(f"  => Result: General (confidence {confidence:.2%} < threshold {CONFIDENCE_THRESHOLD:.0%})")
        return ClassifyResponse(queue="General", confidence=confidence)

    logger.info(f"  => Result: {predicted_queue} (confidence {confidence:.2%})")
    return ClassifyResponse(queue=predicted_queue, confidence=confidence)

@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": model is not None}
