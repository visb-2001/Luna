import nltk
from nltk.stem.lancaster import LancasterStemmer
from nltk.stem.snowball import SnowballStemmer

# nltk.download('punkt')

stemmertest = LancasterStemmer()
stemmer = SnowballStemmer("english")

import numpy
import tensorflow
import random
import json
import re

with open("intents.json") as file:
    data = json.load(file)

words = []
labels = []
docs_x = []
docs_y = []

for intent in data["intents"]:
    for pattern in intent["patterns"]:
        wrds = re.sub("[^A-Za-z0-9 ]+", '', pattern)
        wrds = nltk.word_tokenize(wrds)
        words.extend(wrds)
        docs_x.append(wrds)
        docs_y.append(intent["tag"])

    if intent["tag"] not in labels:
        labels.append(intent["tag"])

words = [stemmer.stem(w.lower()) for w in words if w != "?"]
words = sorted(list(set(words)))

labels = sorted(labels)

training = []
output = []

out_empty = [0 for _ in range(len(labels))]

for x, doc in enumerate(docs_x):
    bag = []

    wrds = [stemmer.stem(w.lower()) for w in doc]

    for w in words:
        if w in wrds:
            bag.append(1)
        else:
            bag.append(0)

    output_row = out_empty[:]
    output_row[labels.index(docs_y[x])] = 1

    training.append(bag)
    output.append(output_row)

training = numpy.array(training)
output = numpy.array(output)

# tensorflow.reset_default_graph()
print(len(training[0]))
model = tensorflow.keras.Sequential([
    tensorflow.keras.layers.Dense(8, input_shape=(len(training[0]),)),
    tensorflow.keras.layers.Dense(8),
    tensorflow.keras.layers.Dense(len(output[0]), activation="softmax"),
])

model.compile(optimizer="adam", loss="categorical_crossentropy", metrics=["accuracy"])
model.fit(training, output, epochs=1000, batch_size=8)
tensorflow.keras.models.save_model(model, "test.h5")
converter = tensorflow.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the TF Lite model.
with tensorflow.io.gfile.GFile('model.tflite', 'wb') as f:
    f.write(tflite_model)


def bag_of_words(s, words):
    bag = [0 for _ in range(len(words))]

    s_words = nltk.word_tokenize(s)
    s_words = [stemmer.stem(word.lower()) for word in s_words]

    for se in s_words:
        for i, w in enumerate(words):
            if w == se:
                bag[i] = 1

    return numpy.array(bag).reshape(-1, len(bag))


def chat():
    print(stemmer.stem("anyone"))
    print("Start talking with the bot (type quit to stop)!")
    while True:
        inp = input("You: ")
        if inp.lower() == "quit":
            break

        print(len(words))
        print(words)
        print(labels)
        print(bag_of_words(inp, words))
        results = model.predict([bag_of_words(inp, words)])
        print(results)
        results_index = numpy.argmax(results)
        tag = labels[results_index]

        for tg in data["intents"]:
            if tg['tag'] == tag:
                responses = tg['responses']

        print(random.choice(responses))


chat()
