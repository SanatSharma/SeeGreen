from watson_developer_cloud import NaturalLanguageUnderstandingV1
import watson_developer_cloud.natural_language_understanding.features.v1 as \
    features

NLU = NaturalLanguageUnderstandingV1(
    version="2017-04-21",
    username="15130ccd-bc58-4208-a2b4-c58239f7aa0c",
    password="YztLOWFs5ZoF"
)

def main():
    with open("compost.txt", "r") as f:
        strings = f.read().split()
    while (strings):
        response = NLU.analyze(
            text = strings.pop(0),
            features = [features.Keywords(), features.Categories(), features.Concepts()]
        )
        print str(response.keywords[0].text)

main()
