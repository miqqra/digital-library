import json


class EntityId:
    def __init__(self, entity_id):
        self.id = entity_id
        self.has_p279 = False
        self.has_p31 = False


class Entity:
    def __init__(self, label):
        self.ids = []
        self.superclasses = []
        self.label = label
        self.semantically_similar = []

    def set_ids(self, ids):
        self.ids = ids


class Superclass:
    def __init__(self, label: str, superclass_id: str):
        self.label = label
        self.superclass_id = superclass_id

    def __eq__(self, other):
        return self.superclass_id == other.superclass_id

    def __hash__(self):
        return hash((self.label, self.superclass_id))


class TemplateLoader:
    def __init__(self, templates_path: str):
        self.templates = json.load(open(templates_path, 'r'))

    def get_template(self, entity: str) -> str:
        template = self.templates['everything']
        return template.replace("ENTITY_ID", entity)


