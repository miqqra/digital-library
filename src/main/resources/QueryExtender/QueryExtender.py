import requests
import json
from Preprocessor import Preprocessor
from classes import *
class QueryExtender:

    @staticmethod
    def execute_sparql_query(query):
        endpoint_url = "https://query.wikidata.org/sparql"
        params = {
            'query': query,
            'format': 'json'
        }
        response = requests.get(endpoint_url, params=params)
        if response.status_code == 200:
            print(f"Status: {response.status_code}")
            return response.json()
        else:
            print(f"Error: {response.status_code}")
            return None
    # Получить список id сущностей по метке
    @staticmethod
    def get_entities_by_label(label: str):
        api_url = "https://www.wikidata.org/w/api.php"
        params = {
            "action": "wbsearchentities",
            "format": "json",
            "language": "ru",
            "search": label,
            "limit": 50,
            "strictlanguage": "false",
            "uselang": "ru",
        }
        response = requests.get(api_url, params=params)
        data = response.json()
        if "search" in data:
            return [EntityId(entity["id"]) for entity in data["search"] if entity["label"][1:] == label[1:]]
        return []

    @staticmethod
    def get_superclasses_by_entity_id(entity_id: EntityId):
        response = requests.get(f'https://www.wikidata.org/wiki/Special:EntityData/{entity_id.id}.json')
        data = response.json()
        actual_entity_id = list(data['entities'].keys())[0]
        entity_id.id = actual_entity_id
        if 'P279' in data['entities'][actual_entity_id]['claims']:
            entity_id.has_p279 = True
        if '31' in data['entities'][actual_entity_id]['claims']:
            entity_id.has_p31 = True

        if entity_id.has_p279:
            search_superclasses_strategy = f'wd:{entity_id.id} wdt:P279* ?superclass.'
        else:
            search_superclasses_strategy = f'wd:{entity_id.id} wdt:P31/wdt:P279* ?superclass.'
        sparql_query = f"""
        SELECT DISTINCT ?superclass ?superclassLabel WHERE {{
          {search_superclasses_strategy}
          SERVICE wikibase:label {{ bd:serviceParam wikibase:language "ru". }}
        }}
        """
        response = QueryExtender.execute_sparql_query(sparql_query)
        if not response:
            return []
        data = response['results']['bindings']
        superclasses = []
        for entity in data:
            if 'xml:lang' in entity['superclassLabel']:
                label = entity['superclassLabel']['value']
                s_id = entity['superclass']['value'].split('/')[-1]
                superclasses.append(Superclass(label, s_id))
        return superclasses
    @staticmethod
    def get_similar_words(entity_id: str, template_loader: TemplateLoader) -> list:
        query = template_loader.get_template(entity_id)
        response = QueryExtender.execute_sparql_query(query)
        if not response:
            return []

    @staticmethod
    def parse_query(query: str) -> list:
        prep = Preprocessor(query)
        return prep.get_tokens()
    @staticmethod
    def extend_query(query: str) -> str:
        tokens = QueryExtender.parse_query(query)
        entities = [Entity(token) for token in tokens]
        for entity in entities:
            entity.ids = QueryExtender.get_entities_by_label(entity.label)
        for entity in entities:
            for entity_id in entity.ids:
                entity.superclasses.extend(QueryExtender.get_superclasses_by_entity_id(entity_id))
                entity.superclasses = list(set(entity.superclasses))
        template_loader = TemplateLoader(templates_path="templates.json")
        results = []
        for entity in entities:
            for entity_id in entity.ids:
                pass
        return entities


QueryExtender.extend_query("Путин собака король озеро ковбои")


#info = QueryExtender.execute_sparql_query(query)

#with open('response.json', 'w', encoding='utf-8') as json_file:
    #json.dump(info, json_file, ensure_ascii=False, indent=4)
