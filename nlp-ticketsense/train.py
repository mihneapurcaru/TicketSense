import joblib
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.pipeline import Pipeline
from sklearn.model_selection import cross_val_score
from training_data import tickets

def train():
    texts = [t[0] for t in tickets]
    labels = [t[1] for t in tickets]

    pipeline = Pipeline([
        ('tfidf', TfidfVectorizer(
            ngram_range=(1, 2),
            max_features=5000,
            stop_words='english',
            sublinear_tf=True
        )),
        ('clf', CalibratedClassifierCV(LinearSVC(max_iter=10000), cv=5))
    ])

    scores = cross_val_score(pipeline, texts, labels, cv=5, scoring='accuracy')
    print(f"Cross-validation accuracy: {scores.mean():.2%} (+/- {scores.std():.2%})")

    pipeline.fit(texts, labels)
    joblib.dump(pipeline, 'model.joblib')
    print("Model saved to model.joblib")

if __name__ == '__main__':
    train()
