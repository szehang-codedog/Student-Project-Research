import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {

    T data;
    String type;
    String level;
    int levelID;
    int startPos, endPos;
    TreeNode<T> parent;
    List<TreeNode<T>> children;
    String compOptions;

    public TreeNode(T data, String type, String level, int levelID, int startPos, int endPos, String options) {
        this.data = data;
        this.type = type;
        this.level = level;
        this.levelID = levelID;
        this.startPos = startPos;
        this.endPos = endPos;
        this.compOptions = options;
        this.children = new ArrayList<TreeNode<T>>();
    }
    
    public TreeNode<T> addChild(T child, String type, String level, int levelID, int startPos, int endPos, String options) {
        TreeNode<T> childNode = new TreeNode<T>(child, type, level, levelID, startPos, endPos, options);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

    public void printData()
    {
    	System.out.println(data);
    }
    // other features ...

}