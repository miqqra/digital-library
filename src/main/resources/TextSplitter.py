# -*- coding: windows-1251 -*-


def split_text(start_id: int, text: str) -> list[dict]:
    words = text.split()
    chunks = [' '.join(words[i:i + 350]) for i in range(0, len(words), 350)]
    return [{"id": start_id + i, "text_block": chunks[i]} for i in range(len(chunks))]


def split_texts(texts: dict[str, str]):
    texts_chunks = []
    last_id = 0
    chunk_id_to_book_id: dict[int, str] = dict()
    for key in texts:
        texts_chunks.extend(split_text(last_id, texts[key]))
        for id in range(last_id, len(texts_chunks)):
            chunk_id_to_book_id[id] = key
        last_id = len(texts_chunks)
    return texts_chunks, chunk_id_to_book_id


