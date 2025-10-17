package com.graphinout.foundation.text;

public interface ITextWriter {

    class TextWriterOnStringBuilder implements ITextWriter {

        boolean isFirstLine = true;
        private final StringBuilder sb;

        public TextWriterOnStringBuilder(StringBuilder sb) {
            this.sb=sb;
        }
        public TextWriterOnStringBuilder() {
            this(new StringBuilder());
        }

        public void line(String line) {
            if(!isFirstLine) {
                sb.append("\n");
            }
            isFirstLine = false;
            sb.append(line);
        }

        @Override
        public String toString() {
            return sb.toString();
        }

    }

    void line(String line);

}
