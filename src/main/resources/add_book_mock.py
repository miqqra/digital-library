import sys


def main():
    book_id = sys.argv[1]
    book_file = sys.argv[2]

    with open(book_file, 'r', encoding="cp1251") as f:
        book_text: str = f.read()

    print("True")


if __name__ == "__main__":
    main()
