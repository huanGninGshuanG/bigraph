package domain.vo;

import java.util.*;

public class MenuVo {

	private String id;
	private String menuId;
	private String menuClass;
	private String menuName;
	private String menuUrl;
	private int orderNo;
	private String pid;
	private MenuVo parent;
	private List<MenuVo> child = new ArrayList<MenuVo>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMenuId() {
		return menuId;
	}
	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}
	public String getMenuClass() {
		return menuClass;
	}
	public void setMenuClass(String menuClass) {
		this.menuClass = menuClass;
	}
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getMenuUrl() {
		return menuUrl;
	}
	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}
	public int getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public MenuVo getParent() {
		return parent;
	}
	public void setParent(MenuVo parent) {
		this.parent = parent;
	}
	public List<MenuVo> getChild() {
		return child;
	}
	public void setChild(List<MenuVo> child) {
		this.child = child;
	}
	
	public void sortChildren() {
		if (this.child != null && this.child.size() > 0) {
			Collections.sort(child, new Comparator<MenuVo>() {
				@Override
				public int compare(MenuVo o1, MenuVo o2) {
					int order1 = o1.getOrderNo();
					int order2 = o2.getOrderNo();
					return order1 < order2 ? -1 : (order1 == order2 ? 0 : 1);
				}
			});
			// 对每个节点的下一层节点进行排序
			for (Iterator<MenuVo> it = child.iterator(); it.hasNext();) {
				it.next().sortChildren();
			}
		}
	}
}
