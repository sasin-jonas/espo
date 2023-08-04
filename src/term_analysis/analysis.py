from collections import Counter

import nltk
import csv
from nltk.stem import WordNetLemmatizer
from nltk.corpus import stopwords

nltk.download('wordnet')
nltk.download('punkt')
nltk.download('stopwords')

lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words('english'))

line_count = 0
lemmas = []

with open(f"../data_examples/MU_LF_data.csv") as csv_file:
    csv_reader = csv.DictReader(csv_file, delimiter=';')
    for item in csv_reader:
        if line_count == 0:
            print(f'Column names are {", ".join(item)}')
        if item.pop('annotationLanguage') != 'EN':
            continue

        item = {k: v.strip() for k, v in item.items()}
        tokens = nltk.word_tokenize(item.pop('annotation'))
        for word in tokens:
            word = word.lower()
            if word not in stop_words and word.isalpha():
                lemmas.append(lemmatizer.lemmatize(word))

        line_count += 1
    print(f'Processed {line_count} lines.')

Counter = Counter(lemmas)
most_frequent = Counter.most_common(1000)

with open("freq_analysis.txt", "w") as file:
    for word in most_frequent:
        file.write(str(word[0]) + "\n")

print(most_frequent)
