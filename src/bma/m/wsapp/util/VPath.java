/*
 * Created on 2005-6-12
 *
 */
package bma.m.wsapp.util;

import java.io.File;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * 表达一个虚拟概念路径<br>
 * 使用[/]分割每个层次的路径
 * 
 * @author 关中
 * @since 1.0 (imported from BMA.ESP Project)
 */
public class VPath {

	private String[] path;

	public static final String[] EMPTY_PATH = new String[0];

	private static final VPath ROOT = new VPath();

	private static final VPath EMPTY = new VPath();

	/**
	 * 路径分割符号
	 */
	public static final char separatorChar = '/';

	/**
	 * 路径分割字符串
	 */
	public static final String separator = "/";

	/**
	 * Reg的路径分割字符串
	 */
	public static final String regSeparator = "\\/";

	/**
	 * 表示当前路径
	 */
	public static final String CURRENT_PATH = ".";

	/**
	 * 表示上一层路径
	 */
	public static final String PARENT_PATH = "..";

	public VPath() {
		super();
		this.path = EMPTY_PATH;
	}

	public static String[] split(String path) {
		return split(path, separator);
	}

	public static String[] split(String path, String separator) {
		StringTokenizer st = new StringTokenizer(path, separator, false);
		if (st.countTokens() == 0) {
			return EMPTY_PATH;
		}
		String[] r = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++) {
			r[i] = st.nextToken();
		}
		return r;
	}

	public static VPath create(String fullPath, String separator) {
		if (fullPath == null) {
			return root();
		}

		VPath reVPath = new VPath();
		reVPath.path = split(fullPath, separator);
		return reVPath;
	}

	/**
	 * 按照指定的路径全称创建VPath
	 * 
	 * @param fullPath
	 * @return
	 */
	public static VPath create(String fullPath) {
		return create(fullPath, separator);
	}

	/**
	 * 使用字符串（路径名成）数组创建路径
	 * 
	 * @param paths
	 * @return
	 */
	public static VPath create(String[] paths) {
		VPath returnPath = new VPath();
		if (paths != null) {
			returnPath.path = paths;
		}
		return returnPath;
	}

	public static VPath create(String[] paths, int start, int len) {
		VPath returnPath = new VPath();
		if (paths != null) {
			returnPath.path = new String[len];
			System.arraycopy(paths, start, returnPath.path, 0, len);
		}
		return returnPath;
	}

	/**
	 * 获取根路径
	 * 
	 * @return
	 */
	public static VPath root() {
		return ROOT;
	}

	/**
	 * 在当前的路径后添加子路径
	 * 
	 * @param subPath
	 * @return
	 */
	public VPath add(String[] subPath) {
		if (subPath == null) {
			return this;
		}

		String[] returnPath = new String[this.path.length + subPath.length];

		int idx = 0;
		for (String path : this.path) {
			returnPath[idx++] = path;
		}
		for (String path : subPath) {
			returnPath[idx++] = path;
		}
		VPath reVPath = new VPath();
		reVPath.path = returnPath;
		return reVPath;
	}

	/**
	 * 在当前的路径后添加子路径
	 * 
	 * @param subPath
	 * @return
	 */
	public VPath add(String subPath) {
		return add(split(subPath));
	}

	/**
	 * 在当前的路径后添加子路径
	 * 
	 * @param subpath
	 * @return
	 */
	public VPath add(VPath subpath) {
		return add(subpath.path);
	}

	/**
	 * 获得子路径
	 * 
	 * @param rootPath
	 * @return
	 */
	public VPath subpath(int idx) {
		if (idx >= this.path.length)
			return EMPTY;
		if (idx >= this.path.length)
			return EMPTY;
		return create(path, idx, this.path.length - idx);
	}

	public VPath subpath(int start, int end) {
		if (start >= this.path.length)
			return EMPTY;
		end = end < this.path.length - 1 ? end : this.path.length - 1;
		if (start >= end)
			return EMPTY;
		int len = end - start;
		if (len == 0)
			return EMPTY;
		return create(path, start, len);
	}

	/**
	 * 获取当前路径相对于rootPath的子路径资料（注：不包含rootPath）
	 * 
	 * @param rootPath
	 * @return
	 */
	public VPath subpath(String rootPath) {
		return subpath(rootPath, true);
	}

	/**
	 * 获取当前路径相对于rootPath的子路径资料（注：不包含rootPath）
	 * 
	 * @param rootPath
	 * @param allMatch
	 *            是否需要全部匹配rootPath，如果true则不全部匹配的时候返回null；false则从匹配处返回路径
	 * @return
	 */
	public VPath subpath(String rootPath, boolean allMatch) {
		return subpath(VPath.create(rootPath), allMatch);
	}

	public VPath subpath(VPath rootPath, boolean allMatch) {
		int i;
		for (i = 0; i < rootPath.path.length; i++) {
			boolean m = false;
			if (i < this.path.length && rootPath.path[i] != null
					&& this.path[i] != null
					&& rootPath.path[i].equals(this.path[i])) {
				m = true;
			}
			if (!m) {
				if (allMatch) {
					return null;
				}
				break;
			}
		}
		VPath reVPath = new VPath();
		if (i < this.path.length) {
			reVPath.path = new String[this.path.length - i];
			System.arraycopy(this.path, i, reVPath.path, 0, this.path.length
					- i);
		}
		return reVPath;
	}

	/**
	 * 把当前路径按照分割符切割为字符串数组
	 * 
	 * @param limit
	 *            切割的返回数量
	 * @return
	 */
	public String[] split(int limit) {
		String fullPath = this.toString();
		return fullPath.split(regSeparator, limit);
	}

	public String[] split() {
		return this.path;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean root) {
		return toString(separator, root);
	}

	/**
	 * 获取该路径的字符串形式
	 */
	public String toString(String sp, boolean root) {
		StringBuffer fullPath = new StringBuffer(80);
		if (root) {
			fullPath.append(sp);
		}
		for (int i = 0; i < this.path.length; i++) {
			if (i != 0) {
				fullPath.append(sp);
			}
			fullPath.append(this.path[i]);
		}
		return fullPath.toString();
	}

	/**
	 * 获取路径名称
	 * 
	 * @return
	 */
	public String getName() {
		int num = this.path.length;
		if (num > 0) {
			return this.path[num - 1];
		} else {
			return "";
		}
	}

	public VPath setName(String name) {
		if (this.path.length > 0) {
			VPath np = copy();
			np.path[np.path.length - 1] = name;
			return np;
		} else {
			return create(name);
		}
	}

	public String getPathName(int idx) {
		if (idx < this.path.length) {
			return this.path[idx];
		}
		return null;
	}

	/**
	 * 获取父路径
	 * 
	 * @return
	 */
	public VPath getParent() {
		VPath repath = new VPath();
		int num = this.path.length - 1;
		if (num > 0) {
			String[] strPath = new String[num];
			for (int i = 0; i < num; i++) {
				strPath[i] = this.path[i];
			}
			repath.path = strPath;
			return repath;
		} else {
			return root();
		}
	}

	/**
	 * 
	 * @param child
	 * @return
	 */
	public boolean isParantOf(VPath child) {
		int level = 0;
		for (; level < this.path.length; level++) {
			if (level >= child.path.length) {
				return false;
			}
			if (!(child.path[level] != null && this.path[level] != null && child.path[level]
					.equals(this.path[level]))) {
				return false;
			}
		}
		return true;
	}

	/*
	 * test rather the paths are equals
	 */
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof VPath) {
			VPath objPath = (VPath) obj;
			if (objPath.path.length != this.path.length) {
				return false;
			}
			for (int i = 0; i < this.path.length && i < objPath.path.length; i++) {
				if (!objPath.path[i].equals(this.path[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * 测试是否是绝对路径，即从'/'开始
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isRooted(String path) {
		if (path.length() > 0 && path.charAt(0) == separatorChar) {
			return true;
		}
		return false;
	}

	public boolean isRoot() {
		return this.pathLevel() == 0;
	}

	/**
	 * 根据当前路径,分析指定的路径<br>
	 * 若paths是从根开始，则返回paths的VPath形式；否则，把paths增加到this的路径下<br>
	 * 
	 * @param parsePath
	 *            需要分析的目录
	 * @return
	 */
	public VPath resolve(String paths) {
		if (VPath.isRooted(paths)) {
			return create(paths);
		} else {
			return add(paths);
		}
	}

	/**
	 * 如果路径层次中有[.]则跳过,[..]则返回上一层
	 * 
	 * @return
	 */
	public VPath parse() {

		boolean parse = false;
		for (String p : this.path) {
			if (p.equals(CURRENT_PATH) || p.equals(PARENT_PATH)) {
				parse = true;
				break;
			}
		}

		if (!parse) {
			return this;
		}

		LinkedList<String> returnPath = new LinkedList<String>();
		for (String p : this.path) {
			if (p.equals(CURRENT_PATH)) {
				continue;
			}
			if (p.equals(PARENT_PATH)) {
				if (!returnPath.isEmpty()) {
					returnPath.removeLast();
				}
				continue;
			}
			returnPath.add(p);
		}
		return create(returnPath.toArray(new String[returnPath.size()]));
	}

	/**
	 * 根据当前路径,获取解析到(resolve)指定的路径所需的路径字符串<br>
	 * 
	 * VPath("/t1/t2/t3").resolve("/t1/t2/t4") = "../t4"
	 * VPath("/t1/t2/t3").resolve("/t1/t2/t3/t4") = "t4"
	 * 
	 * basePath.resolve(basePath.getRelative(path1)).parse() == path1
	 * 
	 * @param path
	 * @return
	 */
	public String getRelative(VPath relPath) {
		return getRelative(relPath, true);
	}

	public String getRelative(String relPath, boolean meetAtRoot) {
		return getRelative(VPath.create(relPath), meetAtRoot);
	}

	public String getRelative(VPath relPath, boolean meetAtRoot) {
		int level = 0; // 相同的层次
		for (; level < relPath.path.length && level < this.path.length; level++) {
			if (!relPath.path[level].equals(this.path[level])) {
				break;
			}
		}

		if (level == 0 && meetAtRoot) {
			// meet at ROOT!!
			return relPath.toString();
		}

		StringBuffer buf = new StringBuffer(80);
		// build PARENT_PATHs
		for (int i = level; i < this.path.length; i++) {
			if (buf.length() > 0) {
				buf.append(separatorChar);
			}
			buf.append(PARENT_PATH);
		}
		// build sub path
		for (int i = level; i < relPath.path.length; i++) {
			if (buf.length() > 0) {
				buf.append(separatorChar);
			}
			buf.append(relPath.path[i]);
		}

		return buf.toString();
	}

	/**
	 * 获取路径的长度
	 * 
	 * @return
	 */
	public int pathLevel() {
		return this.path.length;
	}

	public VPath copy() {
		String[] newPath = new String[this.path.length];
		for (int i = 0; i < this.path.length; i++) {
			newPath[i] = this.path[i];
		}
		return VPath.create(newPath);
	}

	public VPath appendName(String name) {
		VPath np = copy();
		if (np.path.length != 0) {
			np.path[np.path.length - 1] = np.path[np.path.length - 1] + name;
		}
		return np;
	}

	public VPath addExt(String extName) {
		return appendName(extName);
	}

	public VPath setExt(String extName) {
		VPath np = copy();
		if (np.path.length != 0) {
			String name = np.path[np.path.length - 1];
			if (name != null) {
				int idx = name.lastIndexOf('.');
				if (idx != -1) {
					name = name.substring(0, idx) + extName;
				} else {
					name = name + extName;
				}
				np.path[np.path.length - 1] = name;
			}
		}
		return np;
	}

	public String getExt() {
		String name = getName();
		if (name != null && name.length() > 0) {
			int idx = name.lastIndexOf('.');
			if (idx != -1) {
				return name.substring(idx);
			}
		}
		return "";
	}

	public File bindRootFile(File root) {
		return new File(root, toString(false));
	}

	public File bindRootFile(String root) {
		return new File(root, toString(false));
	}
}
