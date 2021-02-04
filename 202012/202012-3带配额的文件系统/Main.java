import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static class MyFile {
        Map<String, MyFile> map = new HashMap<>();//孩子文件
        String fileName;   //文件名
        long lDSize = 0; //本目录下直接的孩子文件占用额度 不含子目录
        long size = 0; //文件大小，或者目录下所有孩子文件占用大小（一直到叶子）
        long lRSize = 0;  //本目录下所有文件大小
        boolean isDirectory = false;
        long LD = 0;//目录配额
        long LR = 0;//后代配额

        //文件
        public MyFile(String fileName, long fileSize) {
            this.fileName = fileName;
            this.size = fileSize;
        }

        //创建目录
        public MyFile(String fileName) {
            this.fileName = fileName;
            this.isDirectory = true;
        }

        //不存在父目录，则创建
        public MyFile findParent(String[] filePath, int index, List<MyFile> list) {
            if (index == filePath.length - 2) {
                return this;
            } else {
                //验证是否有重名
                MyFile file = map.get(filePath[++index]);
                if (file != null && !file.isDirectory) {//若要创建的目录文件与已有的同一双亲目录下的孩子文件中的普通文件名称重复，则该指令不能执行成功。
                    return null;
                }
                if (file == null) { //当路径中的任何目录不存在时，应当尝试创建这些目录；
                    file = new MyFile(filePath[index]);
                    map.put(filePath[index], file);
                }
                list.add(this);
                return file.findParent(filePath, index, list);
            }
        }

        //创建文件  先找到文件的所有祖辈路径、判断要覆盖还是新建、然后根据变动的文件大小验证配额，配额足够才真正创建文件、更新配额
        public boolean create(String[] filePath, int index, String fileName, long fileSize) {
            //另外，还需要确定在该指令的执行是否会使该文件系统的
            //配额变为不满足，如果会发生这样的情况，则认为该指令不能执行成功，反之则认为该指令能执行成功。
            List<MyFile> list = new ArrayList<>();
            MyFile parent = findParent(filePath, index, list);

            if (parent == null) {
                return false;
            }
            MyFile file = parent.map.get(fileName);
            if (file != null && file.isDirectory) {//若路径所指文件已经存在，但是目录文件的，则该指令不能执行成功。
                return false;
            }
            long changeSize = 0;
            if (file == null) {//不存在该文件，则要创建
                file = new MyFile(fileName, fileSize);
                changeSize = fileSize;
            } else {//替换文件，更新大小
                changeSize = fileSize - file.size;
            }
            //在list中做一遍配额校验
            if (changeSize > 0) {//父目录检查 目录配额 和后代配额
                if ((parent.LD != 0 && parent.lDSize + changeSize > parent.LD) ||
                        (parent.LR != 0 && parent.lRSize + changeSize > parent.LR)) {
                    return false;
                }
                for (MyFile pp : list) {
                    if (pp.LR != 0 && pp.lRSize + changeSize > pp.LR) {
                        return false;
                    }
                }
            }
            //都合适才能进行文件更新然后再次循环进行配额更新
            file.size = fileSize;
            parent.map.put(fileName, file);
            if (changeSize != 0) {
                //父目录要更新LD
                parent.lDSize += changeSize;
                parent.size += changeSize;
                parent.lRSize += changeSize;
                for (MyFile pp : list) {//祖辈目录不要更新LD
                    pp.size += changeSize;
                    pp.lRSize += changeSize;
                }
            }
            return true;
        }

        //删除的可能是文件，也可能是目录
        public MyFile remove(String[] filePath, int index, String fileName) {
            MyFile file = null;
            //若该路径所指的文件不存在，则不进行任何操作。
            if (index == filePath.length - 2) {//在当前路径下
                file = map.remove(fileName);
                if (file != null) {
                    //在上述过程中被移除的目录（如果有）上设置的配额值也被移除。
                    if (!file.isDirectory) {//移除的是文件需要更新父目录的LD  移除目录的情况，父路径不需要更新 LDSize
                        this.lDSize -= file.size;
                    }
                    this.size -= file.size;
                    this.lRSize -= file.size;
                    file.map.clear();
                }
            } else {//还需要继续向下找路径
                file = map.get(filePath[++index]);
                if (file != null && file.isDirectory) {
                    file = file.remove(filePath, index, fileName);
                    if (file != null) {
                        this.size -= file.size;
                        this.lRSize -= file.size;
                    }
                } else {//当路径中的任何目录不存在时
                    file = null;
                }
            }
            return file;
        }

        private boolean setQuote(long LD, long LR) {
            //特别地，若配额值为 0，则表示不对该项配额进行限制。
            if ((LD != 0 && lDSize > LD) || (LR != 0 && lRSize > LR)) {
                // 若在应用新的配额值后，该文件系统配额变为不满足，那么该指令执行不成功。
                return false;
            }
            //若在该目录上已经设置了配额，则将原配额值替换为指定的配额值。
            this.LD = LD;
            this.LR = LR;
            return true;
        }

        //设置配额
        public boolean quote(String[] filePath, int index, long LD, long LR) {
            //该指令表示对所指的目录文件，分别设置目录配额和后代配额。
            if (filePath.length == 0 || index == filePath.length - 1) { //操作根路径
                return setQuote(LD, LR);
            }
            MyFile file = map.get(filePath[++index]);
            if (file == null || !file.isDirectory) {
                //若路径所指的文件不存在，或者不是目录文件，则该指令执行不成功。
                return false;
            }
            return file.quote(filePath, index, LD, LR);
        }
    }

    public static void main(String[] args) {
        MyFile root = new MyFile("");
        java.util.Scanner in = new java.util.Scanner(System.in);
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            char op = in.next().charAt(0);
            String filePath = in.next();
            String[] path = filePath.split("/");
            boolean result = false;
            //需要判断该指令能否执行成功
            switch (op) {
                case 'C': //创建
                    //需要创建的普通文件的路径和文件的大小。
                    int fileSize = in.nextInt();
                    result = root.create(path, 0, path[path.length - 1], fileSize);
                    break;
                case 'R': //移除
                    //移除文件的指令有一个参数，是字符串，表示要移除的文件的路径。
                    root.remove(path, 0, path[path.length - 1]);
                    //该指令始终认为能执行成功。
                    result = true;
                    break;
                case 'Q': //设置配额
                    //设置配额值的指令有三个参数，是空格分隔的字符串和两个非负整数，分别表示需要设置配额值的目录的路径、目录配额和后代配额。
                    long LD = in.nextLong();
                    long LR = in.nextLong();
                    result = root.quote(path, 0, LD, LR);
                    break;
            }
            System.out.println(result ? "Y" : "N");
        }
    }
}

