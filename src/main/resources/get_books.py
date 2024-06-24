import json

from add_book_to_index import get_latest_file
from transformers import AutoTokenizer, AutoModel
import torch
import sys
from annoy import AnnoyIndex

def mean_pooling(model_output, attention_mask):
    token_embeddings = model_output[0]
    input_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
    sum_embeddings = torch.sum(token_embeddings * input_mask_expanded, 1)
    sum_mask = torch.clamp(input_mask_expanded.sum(1), min=1e-9)
    return sum_embeddings / sum_mask

def main():
    query = sys.argv[1]

    with open("books_ids.json", 'r') as f:
        book_ids: dict = json.load(f)

    tokenizer = AutoTokenizer.from_pretrained("ai-forever/sbert_large_nlu_ru")
    model = AutoModel.from_pretrained("ai-forever/sbert_large_nlu_ru")
    model.to('cuda')

    encoded_query = tokenizer(query, padding=True, truncation=True, max_length=512, return_tensors='pt')
    encoded_query = encoded_query.to('cuda')

    with torch.no_grad():
        model_output = model(**(encoded_query))
    query_embedding = mean_pooling(model_output, encoded_query['attention_mask'])[0]

    index = AnnoyIndex(1024, 'euclidean')
    latest_index = get_latest_file("D:/study/digital-library-backend/src/main/resources/indices")
    index.load(latest_index)
    results = index.get_nns_by_vector(query_embedding, len(book_ids), include_distances=True)
    #print(index.get_n_items())
    #print(len(results[0]))
    #print(len(book_ids))
    filtered_results = []
    added_books = set()
    for result in results[0]:
        if not (book_ids[str(result)] in added_books):
            filtered_results.append(book_ids[str(result)])
            added_books.add(book_ids[str(result)])
    #for i in range(len(results[0])):
    #print(results[0][i], results[1][i], book_ids[str(results[0][i])])
    with open("D:/study/digital-library-backend/src/main/resources/log.txt", 'w') as f:
        f.write(' '.join(filtered_results))
    print(' '.join(filtered_results))

if __name__ == "__main__":
    main()