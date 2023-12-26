import spacy


class Preprocessor:
    def __init__(self, query: str):
        nlp = spacy.load("ru_core_news_sm")
        doc = nlp(query)
        with doc.retokenize() as retokenizer:
            for ent in doc.ents:
                retokenizer.merge(ent)
        self.tokens = []
        for token in doc:
            if not (token.is_stop or token.is_punct):
                self.tokens.append(token.lemma_)

    def get_tokens(self) -> list:
        return self.tokens
