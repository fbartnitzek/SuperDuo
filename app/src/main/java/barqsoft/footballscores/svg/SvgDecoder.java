package barqsoft.footballscores.svg;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;

import barqsoft.footballscores.Constants;

/**
 * Decodes an SVG internal representation from an {@link InputStream}.
 */
public class SvgDecoder implements ResourceDecoder<InputStream, SVG> {
    public Resource<SVG> decode(InputStream source, int width, int height) throws IOException {
        try {
            SVG svg = SVG.getFromInputStream(source);
            return new SimpleResource<>(svg);
        } catch (SVGParseException ex) {
            throw new IOException(Constants.SVG_ERROR, ex);
        }
    }

    @Override
    public String getId() {
        return "SvgDecoder.com.bumptech.svgsample.app";
    }
}
