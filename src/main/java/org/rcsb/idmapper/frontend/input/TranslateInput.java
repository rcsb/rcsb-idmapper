package org.rcsb.idmapper.frontend.input;

import java.util.List;

//field names must match json input
public class TranslateInput extends Input {
    public Input.Type from;
    public Input.Type to;
    public List<String> ids;
    public List<Input.ContentType> content_type;
}
