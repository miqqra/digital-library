# Сохраните этот код в файле script.py

import sys

def main():
    if len(sys.argv) > 1:
        # Если передан хотя бы один аргумент из командной строки
        input_string = sys.argv[1]
        return input_string
    else:
        return "Пожалуйста, передайте строку в качестве аргумента."

if __name__ == "__main__":
    result = main()
    print(result)