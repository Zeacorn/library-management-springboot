package com.zea.springboot.controller.request;

import lombok.Data;

@Data
public class CategoryPageRequest extends BaseRequest{
    private String name;
}