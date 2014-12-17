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
            {"type" : "table",
            "name" : "The first test table",
            "columns" : ["C1", "C2", "C3","C4"],
            "data" : [
                [1, 2, 3, 4, 5],
                [0, 1, 0, 0, 0],
                [0, 0, 1, 0, 0],
                [0, 0, 0, 1, 5]]},
            {"type" : "text",
            "data" : "Sample text."}
        ]
    },
    {
        "status" : "SUCCESS",
        "path" : [ "A", "A1", "A12"]
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
