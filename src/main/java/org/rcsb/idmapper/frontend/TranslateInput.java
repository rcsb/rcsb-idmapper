package org.rcsb.idmapper.frontend;

//field names must match json input
public class TranslateInput extends Input {
    public String id;
    public Input.Type from;
    public Input.Type to;
    public Input.ContentType content_type;
}
