
package com.os.services.interceptor.pdfextractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfTools
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfTools.class);

    public static void extractPageFromStream(InputStream inputStream, int startPage, int endPage, OutputStream outputStream)
    {
        try
        {
            Splitter splitter = new Splitter();
            splitter.setStartPage(startPage);
            splitter.setEndPage(endPage);
            splitter.setSplitAtPage(endPage - startPage + 1);

            try (PDDocument document = PDDocument.load(inputStream))
            {
                List<PDDocument> documents = splitter.split(document);

                if (documents.size() != 1)
                {
                    throw new IllegalArgumentException("cannot split document, wrong number of split parts");
                }
                try (PDDocument doc = documents.get(0))
                {
                    PdfTools.writeDocument(doc, outputStream);
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.info(ExceptionUtils.getMessage(e));
            throw new IllegalArgumentException(ExceptionUtils.getMessage(e));
        }
    }

    private static void writeDocument(PDDocument doc, OutputStream outputStream) throws IOException
    {
        try (COSWriter writer = new COSWriter(outputStream))
        {
            writer.write(doc);
        }
    }

}
