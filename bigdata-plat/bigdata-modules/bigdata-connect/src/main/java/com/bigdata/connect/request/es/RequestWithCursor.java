package com.bigdata.connect.request.es;

import lombok.Data;

@Data
public class RequestWithCursor extends Request {
    private String cursor;
}
