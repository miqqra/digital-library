import sys
from QueryExtender import *

def main():
    input_query = sys.argv[1]
    extended_query = QueryExtender.extend_query(input_query)
    return extended_query
if __name__ == "__main__":
    result = main()
    print(result)