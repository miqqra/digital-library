from transformers import AutoTokenizer, AutoModel
import torch


def mean_pooling(model_output, attention_mask):
    token_embeddings = model_output[0]
    input_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
    sum_embeddings = torch.sum(token_embeddings * input_mask_expanded, 1)
    sum_mask = torch.clamp(input_mask_expanded.sum(1), min=1e-9)
    return sum_embeddings / sum_mask


def get_embeddings(chunks: list[dict]):
    tokenizer = AutoTokenizer.from_pretrained("ai-forever/sbert_large_nlu_ru")
    model = AutoModel.from_pretrained("ai-forever/sbert_large_nlu_ru")
    model.eval()
    model.to('cuda')
    chunk_embeddings = []
    for i in range(0, len(chunks), 32):
        encoded_chunks = tokenizer([chunk['text_block'] for chunk in chunks[i:i + 32]], padding=True, truncation=True,
                                   max_length=512, return_tensors='pt')
        encoded_chunks = encoded_chunks.to('cuda')
        with torch.no_grad():
            model_output = model(**(encoded_chunks))
        chunk_embeddings.append(mean_pooling(model_output, encoded_chunks['attention_mask']))
    embeddings = torch.concat(chunk_embeddings).cpu()
    embeddings_with_ids = []
    for i in range(len(embeddings)):
        embeddings_with_ids.append((chunks[i]["id"], embeddings[i]))
    return embeddings_with_ids