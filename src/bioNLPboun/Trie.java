package bioNLPboun;

/**
 * Created by berfu on 3.4.2016.
 */
public class Trie {

    public TrieNode root;
    public int minLevDist;

    public Trie() {
        this.root = new TrieNode(' ');
       // Main.trieNodesList.add(root);
    }

    public void insert(String word) {

        int length = word.length();
        TrieNode current = this.root;

        if (length == 0) {
            current.isWord = true;
        }
        for (int index = 0; index < length; index++) {

            char letter = word.charAt(index);
            TrieNode child = current.getChild(letter);

            if (child != null) {
                current = child;
            } else {
                TrieNode trieNode = new TrieNode(letter);
                current.children.put(letter, trieNode);
                current = current.getChild(letter);
               // Main.trieNodesList.add(trieNode);
            }
            if (index == length - 1) {
                current.isWord = true;
            }
        }
    }
}
