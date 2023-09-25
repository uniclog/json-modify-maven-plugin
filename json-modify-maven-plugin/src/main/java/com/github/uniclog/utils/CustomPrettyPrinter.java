package com.github.uniclog.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;

import java.io.IOException;

public class CustomPrettyPrinter extends DefaultPrettyPrinter {

    public CustomPrettyPrinter() {
        _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
        _objectIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    }

    public CustomPrettyPrinter(DefaultPrettyPrinter base) {
        super(base);
    }

    @Override
    public CustomPrettyPrinter createInstance() {
        return new CustomPrettyPrinter(this);
    }

    @Override
    public CustomPrettyPrinter withSeparators(Separators separators) {
        this._separators = separators;
        this._objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
        return this;
    }

    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
        if (!_arrayIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw(']');
    }

    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
        if (!_objectIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw('}');
    }
}