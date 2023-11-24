package visitor;

public interface Formula {
//    int low=0;
//    String valid = "";
//    int dfsN = 0;
//    Formula subGoals();
    Formula convertToCTLBase();//定义了一个接口中的方法，实现该接口的类中实现了这个方法
    Formula convertToENF(); // hns: CTL化简，转换为ENF形式(existential normal form)
    void accept(FormulaVisitor visitor);
}
