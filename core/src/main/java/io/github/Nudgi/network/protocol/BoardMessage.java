package io.github.Nudgi.network.protocol;

import java.util.Arrays;

import io.github.Nudgi.network.protocol.config.SourceContext;

public final class BoardMessage extends Message {
    private final int[] paths;

    /**
     * Creates a new BoardMessage. All BoardMessages
     * always originate from the server.
     * @param paths the paths of the tiles on the board.
     */
    public BoardMessage(int[] paths) {
        super(SourceContext.SERVER);
        this.paths = paths.clone();
    }

    private BoardMessage(SourceContext source, String[] args) {
        super(source);
        this.paths = Arrays.stream(args)
            .mapToInt(Integer::valueOf)
            .toArray();
    }

    public int[] getPaths() {
        return paths;
    }

    @Override 
    protected String[] serialized() {
        return Arrays.stream(paths)
            .boxed()
            .map(String::valueOf)
            .toArray(String[]::new);
    }
}
