package indi.ysgj1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class EmployeeTaskMain2 {

	// 每组最少人数
	private final static int MIN_GROUP_SIZE = 2;

	protected static class GroupInfo {

		String id;

		Employee[] employees;

		int toDoTaskCount, doingTaskCount, doneTaskCount, totalTaskCount;

		int priority1, priority2, priority3, priority4;

		double wt, wtRate;

		public GroupInfo(String id, Employee[] employees) {
			super();
			this.id = id;
			this.employees = employees;

			int maxToDoTaskCount = 0, maxDoingTaskCount = 0, maxDoneTaskCount = 0, maxTotalTaskCount = 0;
			for (Employee employee : this.employees) {
				toDoTaskCount += employee.toDoTaskCount;
				doingTaskCount += employee.doingTaskCount;
				doneTaskCount += employee.doneTaskCount;
				totalTaskCount += employee.totalTaskCount;

				maxDoingTaskCount = maxDoingTaskCount > employee.doingTaskCount ? maxDoingTaskCount
						: employee.doingTaskCount;
				maxDoneTaskCount = maxDoneTaskCount > employee.doneTaskCount ? maxDoneTaskCount
						: employee.doneTaskCount;
				maxToDoTaskCount = maxToDoTaskCount > employee.toDoTaskCount ? maxToDoTaskCount
						: employee.toDoTaskCount;

				maxTotalTaskCount = maxTotalTaskCount > employee.totalTaskCount ? maxTotalTaskCount
						: employee.totalTaskCount;
			}

			totalTaskCount += maxTotalTaskCount;
			toDoTaskCount += maxToDoTaskCount;
			doneTaskCount += maxDoneTaskCount;
			doingTaskCount += maxDoingTaskCount;

		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.id;
		}

		// （1）优先选派组内所有人员都无正在执行的任务的组合；
		// （所有人员都无正在执行的任务的组合*5；其他组合*1）
		public void updatePriority1() {
			int p = 5;
			for (Employee employee : this.employees) {
				if (employee.doingTaskCount > 0) {
					p = 1;
					break;
				}
			}
			priority1 = p;
		}

		// （2）优先选择当前任务的监管点与人员所在的监管点（有正在执行的任务时选择正在执行任务的监管点；无当前正在执行的任务，判断最新被派单的任务的监管点）相一致的组合（限定水运）；
		// （组内所有人员都是监管点一致（或无监管点）的*4；组内部分人员监管点一致（或无监管点） 另一部分人不一致的*2；所有人监管点都不一致的*1）
		public void updatePriority2(String portCode) {
			int p = 4;
			for (Employee employee : this.employees) {
				if (!portCode.equals(employee.portCode) && employee.portCode != null) {
					p = 1;
					break;
				}
			}

			if (p == 1) {

				for (Employee employee : this.employees) {
					if (portCode.equals(employee.portCode) || employee.portCode == null) {
						p = 2;
						break;
					}
				}

			}

			priority2 = p;
		}

		// （3）优先选择待办任务较少的组合；
		// （对所有组合的待办任务数进行计算并排序，计算方法为组内所有人员待办任务数总和+组内待办任务数最多的人员的任务数；按从多到少赋予的排序值设定权重）
		public void updatePriority3(List<GroupInfo> groupInfos) {
			int p = 0;
			Collections.sort(groupInfos, new Comparator<GroupInfo>() {

				@Override
				public int compare(GroupInfo o1, GroupInfo o2) {
					// TODO Auto-generated method stub
					return o2.toDoTaskCount - o1.toDoTaskCount;
				}
			});
			for (int i = 0; i < groupInfos.size(); i++) {
				if (this.id.equals(groupInfos.get(i).id)) {
					p = i + 1;

					for (int j = 1; j < i; j++) {
						// 回退找最小的序号
						if (groupInfos.get(i).toDoTaskCount == groupInfos.get(i - j).toDoTaskCount) {
							p--;
						} else {
							break;
						}
					}

				}
			}
			priority3 = p;
		}

		// （4）优先选择当天总任务（待办、在办、已办）较少的组合。
		// （对所有组合的当天总任务数进行计算并排序，计算方法为组内所有人员总任务数总和+组内总任务数最多的人员的任务数；按从多到少赋予的排序值设定权重）
		public void updatePriority4(List<GroupInfo> groupInfos) {
			int p = 0;
			Collections.sort(groupInfos, new Comparator<GroupInfo>() {

				@Override
				public int compare(GroupInfo o1, GroupInfo o2) {
					// TODO Auto-generated method stub
					return o2.totalTaskCount - o1.totalTaskCount;
				}
			});
			for (int i = 0; i < groupInfos.size(); i++) {
				if (this.id.equals(groupInfos.get(i).id)) {
					p = i + 1;
					// 回退找最小的序号
					for (int j = 1; j < i; j++) {
						if (groupInfos.get(i).totalTaskCount == groupInfos.get(i - j).totalTaskCount) {
							p--;
						} else {
							break;
						}
					}
				}
			}
			priority4 = p;
		}

		// （4）优先选择当天总任务（待办、在办、已办）较少的组合。
		// （对所有组合的当天总任务数进行计算并排序，计算方法为组内所有人员总任务数总和+组内总任务数最多的人员的任务数；按从多到少赋予的排序值设定权重）
		public void updateWT(List<GroupInfo> groupInfos) {
			int p = this.priority1 * this.priority2 * this.priority3 * this.priority4;
			wt = p;
		}

		public void updateWTRate(List<GroupInfo> groupInfos) {
			double totalWt = 0;
			for (GroupInfo groupInfo : groupInfos) {
				totalWt += groupInfo.wt;
			}
			// System.out.println(totalWt);
			wtRate = this.wt / totalWt;
		}

		protected static GroupInfo random(Map<String, GroupInfo> groupInfos) {
			GroupInfo re = null;
			Random random = new Random();

			Map<String, int[]> wtMap = new HashMap<>();
			int wt = 0;
			for (GroupInfo groupInfo : groupInfos.values()) {
				wtMap.put(groupInfo.id, new int[] { wt, wt += groupInfo.wt });
			}

			int rWt = random.nextInt(wt);

			for (String groupId : wtMap.keySet()) {

				int[] rang = wtMap.get(groupId);

				if (rWt > rang[0] && rWt < rang[1]) {
					re = groupInfos.get(groupId);
				}

			}

			return re;
		}
	}

	// 人员类
	protected static class Employee {

		String id, portCode;

		Set<String> skillIds = new HashSet<>();

		int toDoTaskCount, doingTaskCount, doneTaskCount, totalTaskCount;

		public Employee(String id, String... skillIds) {
			super();
			this.id = id;
			for (String skillId : skillIds) {
				this.skillIds.add(skillId);
			}
		}

		// 是否满足技能
		int hasSkill(String... skillIds) {
			int re = 0;
			for (String skillId : skillIds) {
				if (this.skillIds.contains(skillId)) {
					re++;
				}
			}
			return re;
		}

		protected static int hasSkill(Employee[] employees, String... skillIds) {
			int re = 0;
			for (String skillId : skillIds) {
				for (Employee employee : employees) {
					if (employee.skillIds.contains(skillId)) {
						re++;
						break;
					}
				}
			}
			return re;
		}

		// 按人员id排序生成组id
		protected static String genGroupId(Employee... employees) {
			String re = "";
			String[] str = new String[employees.length];

			for (int i = 0; i < str.length; i++) {
				str[i] = employees[i].toString();
			}
			Arrays.sort(str);
			for (int i = 0; i < str.length; i++) {
				re += str[i] + "|";
			}
			return re;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.id;
		}

		protected static Employee[] appendTo(Employee[] employees, Employee... ems) {
			Set<Employee> re = new HashSet<>();
			for (Employee employee : employees) {
				re.add(employee);
			}
			for (Employee employee : ems) {
				re.add(employee);
			}
			return re.toArray(new Employee[re.size()]);
		}

		protected static int isContain(Employee[] employees, Employee[] subEmployees) {
			int re = 0;

			for (Employee subEmployee : subEmployees) {
				try {
					for (Employee employee : employees) {

						if (subEmployee == employee) {
							re++;
							break;
						}

					}
				} catch (Exception ex) {
					// ex.printStackTrace();
				}
			}

			return re;
		}

		protected void updateCur(int toDoTaskCount, int doingTaskCount, int doneTaskCount, String portCode) {
			this.toDoTaskCount = toDoTaskCount;
			this.doingTaskCount = doingTaskCount;
			this.doneTaskCount = doneTaskCount;
			this.portCode = portCode;
			this.totalTaskCount = this.toDoTaskCount + this.doingTaskCount + this.doneTaskCount;
		}

	}

	// 算法入口
	private static Map<String, Employee[]> calEmployeeGroupBySkill(List<Employee> allEmployees,
			String... needSkillIds) {
		Map<String, Employee[]> candidateEmployeeGroupMap = new HashMap<>(), addEmployeeGroupMap = new HashMap<>();
		;

		// 候选组人员
		Set<Employee> candidateEmployees = new HashSet<>();
		for (Employee employee : allEmployees) {
			if (employee.hasSkill(needSkillIds) > 0) {
				// 至少符合一个需求技能才能加入候选组
				candidateEmployeeGroupMap.put(Employee.genGroupId(employee), new Employee[] { employee });
				candidateEmployees.add(employee);
			}

		}

		for (int i = 2; i <= needSkillIds.length; i++) {
			// 两人小组开始计算符合要求的组合
			Map<String, Employee[]> calEmployeeGroupMap = _calEmployeeGroupBySkill(candidateEmployees,
					candidateEmployeeGroupMap, i, needSkillIds);
			addEmployeeGroupMap.putAll(calEmployeeGroupMap);
		}

		// 分级剪纸，类似购物篮的剪枝
		for (int i = 2; i <= needSkillIds.length; i++) {
			Set<String> groupEqiIds = new HashSet<>(), groupGtiIds = new HashSet<>();
			for (String groupId : addEmployeeGroupMap.keySet()) {
				Employee[] addEmployees = addEmployeeGroupMap.get(groupId);
				if (addEmployees.length == i) {
					groupEqiIds.add(groupId);
				} else if (addEmployees.length > i) {
					groupGtiIds.add(groupId);
				}
			}

			for (String groupGtiId : groupGtiIds) {

				for (String groupEqiId : groupEqiIds) {

					if (Employee.isContain(addEmployeeGroupMap.get(groupGtiId),
							addEmployeeGroupMap.get(groupEqiId)) >= i) {
						addEmployeeGroupMap.remove(groupGtiId);
					}

				}

			}

		}

		return addEmployeeGroupMap;

	}

	private static Map<String, Employee[]> _calEmployeeGroupBySkill(Set<Employee> candidateEmployees,
			Map<String, Employee[]> candidateEmployeeGroupMap, int groupSize, String... needSkillIds) {
		Map<String, Employee[]> addEmployeeGroupMap = new HashMap<>();
		String[] groupIds = candidateEmployeeGroupMap.keySet().toArray(new String[candidateEmployeeGroupMap.size()]);
		for (String groupId : groupIds) {
			Employee[] employees = candidateEmployeeGroupMap.get(groupId);
			if (employees.length != groupSize - 1)
				continue;

			for (Employee candidateEmployee : candidateEmployees) {
				Employee[] newEmpGroup = Employee.appendTo(employees, candidateEmployee);
				String newGroupId = Employee.genGroupId(newEmpGroup);
				if (Employee.hasSkill(newEmpGroup, needSkillIds) >= needSkillIds.length
						&& newEmpGroup.length >= MIN_GROUP_SIZE) {
					// 满足所有技能加入执行组
					addEmployeeGroupMap.put(newGroupId, newEmpGroup);
				} else {
					// 未满足所有技能加入候选组
					candidateEmployeeGroupMap.put(newGroupId, newEmpGroup);
				}
			}

		}

		return addEmployeeGroupMap;
	}

	private static List<GroupInfo> calGroupInfo(List<Employee> allEmployees, String portCode, String... needSkillIds) {
		List<GroupInfo> groupInfos = new ArrayList<>();

		Map<String, Employee[]> employeeGroupMap = calEmployeeGroupBySkill(allEmployees, needSkillIds);

		for (String key : employeeGroupMap.keySet()) {
			GroupInfo groupInfo = new GroupInfo(key, employeeGroupMap.get(key));
			groupInfos.add(groupInfo);
		}

		for (int i = 0; i < groupInfos.size(); i++) {
			GroupInfo groupInfo = groupInfos.get(i);
			groupInfo.updatePriority1();
			groupInfo.updatePriority2(portCode);
			groupInfo.updatePriority3(groupInfos);
			groupInfo.updatePriority4(groupInfos);
			groupInfo.updateWT(groupInfos);
		}

		for (int i = 0; i < groupInfos.size(); i++) {
			GroupInfo groupInfo = groupInfos.get(i);

			groupInfo.updateWTRate(groupInfos);
		}
		return groupInfos;
	}

	private static List<Employee> loadEmployees() {

		List<Employee> employees = new ArrayList<>();

		employees.add(new Employee("1", "A", "C"));
		employees.add(new Employee("2", "A", "B", "D"));
		employees.add(new Employee("3", "E"));
		employees.add(new Employee("4", "A", "C", "D", "E"));
		employees.add(new Employee("5", "B"));
		employees.add(new Employee("6", "D"));
		employees.add(new Employee("7", "C"));
		employees.add(new Employee("8", "B", "D", "E"));

		employees.get(0).updateCur(4, 0, 2, "b");
		employees.get(1).updateCur(3, 1, 3, "b");
		employees.get(2).updateCur(2, 2, 4, "a");
		employees.get(3).updateCur(2, 0, 2, "a");
		employees.get(4).updateCur(1, 1, 3, "b");
		employees.get(5).updateCur(0, 2, 4, "b");
		employees.get(6).updateCur(4, 1, 3, "b");
		employees.get(7).updateCur(0, 0, 2, null);

		return employees;

	}

	public static void main(String[] args) {

		List<Employee> employees = loadEmployees();

	
		
		String[] rows=new String[] {
			"a-A,C,E"
			,"b-A,B,C"
			,"a-E,B"
			,"c-A,D"
			,"a-A,D"
			,"b-B,E"
		};

		for (String row : rows) {
			String port=row.split("-")[0];
			String[] needSkills = row.split("-")[1].split(",");
			System.out.println("\n\n申请任务   监管点:" + port + "  技能:" + String.join(",", needSkills));
			List<GroupInfo> groupInfos = calGroupInfo(employees, port, needSkills);
			Map<String, GroupInfo> groupInfoMap = new HashMap<>();

			for (GroupInfo groupInfo : groupInfos) {
				groupInfoMap.put(groupInfo.id, groupInfo);
				String msg = MessageFormat.format("id:{0}, {1} , {2},  {3}, {4}, {5} , {6}, {7} , {8}", groupInfo.id,
						groupInfo.priority1, groupInfo.priority2, groupInfo.priority3, groupInfo.priority4,
						groupInfo.wt, groupInfo.wtRate, groupInfo.toDoTaskCount, groupInfo.totalTaskCount);
				System.out.println(msg);
			}

			GroupInfo randGroupInfo = GroupInfo.random(groupInfoMap);
			System.out.println("抽中：" + randGroupInfo.id);
			for (Employee groupEmp : randGroupInfo.employees) {
				for (Employee emps : employees) {
					if (emps.id.equals(groupEmp.id)) {
						//更新待办数
						emps.toDoTaskCount++;
						// emps.portCode=port;
					}
				}
			}
		}

	}

}
