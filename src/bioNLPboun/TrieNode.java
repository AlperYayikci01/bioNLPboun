package bioNLPboun;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by berfu on 3.4.2016.
 */
public class TrieNode {

    public final int ALPHABET = 26;

    public Names nameObj;
    public char letter;
    public boolean isWord;
    public Map<Character, TrieNode> children;

    public TrieNode(char letter) {
        this.isWord = false;
        this.letter = letter;
        children = new HashMap<Character, TrieNode>(ALPHABET);

    }

    public TrieNode getChild(char letter) {

        if (children != null) {
            if (children.containsKey(letter)) {
                return children.get(letter);
            }
        }
        return null;
    }
}
