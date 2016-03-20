package edp.javac.compiler;

import java.util.List;

// supported API
import com.sun.source.tree.Tree;

public interface Parser {
    List<Tree> parse(String source);
}
