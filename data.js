var data=[
    {
        "status" : "SUCCESS",
        "path" : [ "A", "A1", "A11", "T1"],
        "fails" : [
                {"type" : "A.E.E",
                "msg" : "AAAAA Error Assert Error"},
                {"type" : "B.E.E",
                "msg" : "BBBBB Error Assert Error"},
                {"type" : "C.E.E",
                "msg" : "CCCCC Error Assert Error"}
            ],
        "meta" : [
            {"type" : "text",
            "data" : "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam vel purus et neque elementum aliquet. Fusce in elementum turpis. Curabitur nec elementum libero. Donec sit amet arcu sit amet nisi elementum blandit a nec velit. Ut augue dui, viverra pulvinar ligula sed, blandit vulputate ante. Duis suscipit neque arcu, in consequat velit posuere vitae. Aliquam malesuada viverra risus non pharetra. Vivamus accumsan leo vel auctor placerat. Morbi interdum eget lacus at euismod. Nunc ornare semper congue. Nam nisi arcu, tempor nec nisi sed, vehicula efficitur massa. Mauris condimentum odio pulvinar pharetra faucibus. Duis hendrerit vitae augue a varius."},
            {"type" : "table",
            "name" : "The first test table",
            "columns" : ["C1", "C2", "C3","C4"],
            "data" : [
                [1, 2, 3, 4, 5],
                [0, 1, 0, 0, 0],
                [0, 0, 1, 0, 0],
                [0, 0, 0, 1, 5]]}
        ]
    },
    {
        "status" : "SUCCESS",
        "path" : [ "A", "A1", "A12"]
    },
    {
        "status" : "SUCCESS",
        "path" : [ "A", "A1", "A11", "T2"]
    },
    {
        "status" : "FAIL",
        "path" : [ "A", "A2"]
    },
    {
        "status" : "SUCCESS",
        "path" : [ "B", "B1"]
    },
    {
        "status" : "FAIL",
        "path" : [ "C", "C1", "C11"]
    },
    {
        "status" : "SUCCESS",
        "path" : [ "C", "C2", "C21"]
    }

]
