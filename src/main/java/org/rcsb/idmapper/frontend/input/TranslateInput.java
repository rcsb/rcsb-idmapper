package org.rcsb.idmapper.frontend.input;

import org.rcsb.common.constants.ContentType;

import java.util.List;

//field names must match json input
public class TranslateInput extends Input {
    public Input.Type from;
    public Input.Type to;
    public List<String> ids;
    public List<ContentType> content_type;
}
