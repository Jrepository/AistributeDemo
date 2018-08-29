package indi.ysgj3;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class EmployeeTaskMainV2 {

	// 每组最少人数
	private final static int MIN_GROUP_SIZE = 2;
	
	private final static double[] WTS=new double[] {0.4,0.2,0.2,0.2};

	
	protected static class TaskInfo{
		
		int seqNo;
		
		GroupInfo groupInfo;
		
		boolean isAsigned=false,isDone=false;

		String portCode;
		
		String remark;
		
		String[] skills;
		
		public TaskInfo(int seqNo, String portCode, String...skills) {
			super();
			this.seqNo = seqNo;
			this.skills=skills;
			this.portCode=portCode;
		}
		
		
		
	}
	
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
				toDoTaskCount += employee.toDoTasks.size();
				doingTaskCount += employee.doingTasks.size();
				doneTaskCount += employee.doneTasks.size();
				totalTaskCount += employee.totalTaskCount();

				maxDoingTaskCount = maxDoingTaskCount > employee.doingTasks.size() ? maxDoingTaskCount
						: employee.doingTasks.size();
				maxDoneTaskCount = maxDoneTaskCount > employee.doneTasks.size() ? maxDoneTaskCount
						: employee.doneTasks.size();
				maxToDoTaskCount = maxToDoTaskCount > employee.toDoTasks.size() ? maxToDoTaskCount
						: employee.toDoTasks.size();

				maxTotalTaskCount = maxTotalTaskCount > employee.totalTaskCount() ? maxTotalTaskCount
						: employee.totalTaskCount();
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

		/*
		 * 
		 * （1）  有无正在执行的任务：
组合内所有人员都无当前正在执行的任务，计10分；组合内有1人有当前正在执行的任务，计6分；组合内有2人有当前正在执行的任务，计3分；组合内有3人及以上有当前正在执行的任务，计0分；

		 * 
		 */
		public void updatePriority1() {
			int p = 10,count=0;
			for (Employee employee : this.employees) {
				if (employee.doingTasks.size() > 0) {
					count++;
					
				}
			}
			if(count==1) {
				p=6;
			}else if(count==2) {
				p=3;
			}else if(count>=3) {
				p=0;
			}
			
			priority1 = p;
		}

		
		
		/*
		 * 新的待派任务，与组内所有人员都是监管点一致（监管点为空的，视同一致）的，计10分；与组内1人监管点不一致，
		 * 其他人员监管点一致的，计6分；与组内2人监管点不一致，计3分；与组内3人及以上监管点不一致，计3分
		 */
		public void updatePriority2(String portCode) {
			int p = 10,count=0;
			
			for (Employee employee : this.employees) {
				
				String curPortCode=null;
				/*
				 * 人员当前监管点判断方法：系统根据人员当前执行的任务判断人员所处的监管点；
				 
				 * 
				 * 既无当前正在执行的任务、又无派单任务的，监管点为空。
				 */
				if(employee.doingTasks.size()>0) {
					// 有多条当前正在执行的任务时，选择最近一条开始执行的任务判定监管点；
					curPortCode=employee.doingTasks.get(0).portCode;
				}else if(employee.toDoTasks.size()>0) {
					//无当前正在执行的任务时，选择最近一条派单的任务判定监管点；
					curPortCode=employee.toDoTasks.get(0).portCode;
				}
				
				
				if (!portCode.equals(curPortCode) && curPortCode != null) {
					count++;
					
				}
			}

			if(count==1) {
				p=6;
			}else if(count==2) {
				p=3;
			}else if(count>=3) {
				p=0;
			}

			priority2 = p;
		}

	
		/*
		 * 对所有组合的待办任务数进行计算并排序，计算方法为组内所有人员待办任务数总和+组内待办任务数最多的人员的任务数；
		 * 按从少到多的排序赋值，待办工作量最少的计10分，待办工作量第二少的计9分，
		 */
		public static void updatePriority3(List<GroupInfo> groupInfos) {
			int P = 10;
			Collections.sort(groupInfos, new Comparator<GroupInfo>() {

				@Override
				public int compare(GroupInfo o1, GroupInfo o2) {
					// TODO Auto-generated method stub
					return o1.toDoTaskCount - o2.toDoTaskCount;
				}
			});
			for (int i = 0; i < groupInfos.size(); i++) {
				//if (this.id.equals(groupInfos.get(i).id)) {
					int p = P -i;

					for (int j = 1; j <= i; j++) {
						
						if (groupInfos.get(i).toDoTaskCount == groupInfos.get(i - j).toDoTaskCount) {
							p++;
						} else {
							break;
						}
					}
                   groupInfos.get(i).priority3=p;
				//}
			}
			
		}

		/*
		 * 对所有组合的当天总任务数进行计算并排序，计算方法为组内所有人员总任务数总和+组内总任务数最多的人员的任务数；
		 * 按从少到多的排序赋值，总任务量最少的计10分，第二少的计9分，以此类推
		 */
		public static void updatePriority4(List<GroupInfo> groupInfos) {
			int P = 10;
			Collections.sort(groupInfos, new Comparator<GroupInfo>() {

				@Override
				public int compare(GroupInfo o1, GroupInfo o2) {
					// TODO Auto-generated method stub
					return o1.totalTaskCount - o2.totalTaskCount;
				}
			});
			for (int i = 0; i < groupInfos.size(); i++) {
				//if (this.id.equals(groupInfos.get(i).id)) {
					int p = P-i ;
					// 
					for (int j = 1; j <= i; j++) {
						if (groupInfos.get(i).totalTaskCount == groupInfos.get(i - j).totalTaskCount) {
							p++;
						} else {
							break;
						}
					}
					groupInfos.get(i).priority4 = p;
				//}
			}
			
		}

		// （4）优先选择当天总任务（待办、在办、已办）较少的组合。
		// （对所有组合的当天总任务数进行计算并排序，计算方法为组内所有人员总任务数总和+组内总任务数最多的人员的任务数；按从多到少赋予的排序值设定权重）
		public void updateWT(List<GroupInfo> groupInfos) {
			double p = this.priority1 *WTS[0] +
					this.priority2 *WTS[1] +
					this.priority3 *WTS[2] +
					this.priority4 *WTS[3];
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

		/*
		 * 1）当分值存在并列最高时，系统随机在并列组时选择其中一组作为最终派单组；
（2）当分值最高值不存在并列，且分数最高值比次高值高1分及以上的，选择分数最高值组为最终派单组；
（3）当分值最高值不存在并列，且分数最高值比次高值差在1分以内的，最高值和次高值按7：3的概率随机选出最终派单组；

		 */
		protected static GroupInfo draw(Map<String, GroupInfo> groupInfoMap) {
			Random random=new Random();
			GroupInfo re = null,firstGroup=null;
		    List<GroupInfo> groupInfos=new ArrayList<>(groupInfoMap.values())
		    		,topGroupInfos=new ArrayList<>()
		    		,orderGroupInfos=new ArrayList<>()		
		    		;
			Collections.sort(groupInfos, new Comparator<GroupInfo>() {

				@Override
				public int compare(GroupInfo o1, GroupInfo o2) {
					// TODO Auto-generated method stub
					if(o1.wt==o2.wt)return 0;
					else if(o1.wt>o2.wt)return -1;
					return 1;
					
				}
			});
			firstGroup=groupInfos.get(0);
			topGroupInfos.add(firstGroup);
			orderGroupInfos.add(firstGroup);
			for(int i=1;i<groupInfos.size();i++) {
				GroupInfo iGroup=groupInfos.get(i);
				if(iGroup.wt==firstGroup.wt) {
					topGroupInfos.add(iGroup);
				}else {
					orderGroupInfos.add(iGroup);
					break;
				}
				
			}
			String msg="";
			if(topGroupInfos.size()>1) {
				
				int ri=random.nextInt(topGroupInfos.size());
				re=topGroupInfos.get(ri);
				msg="当分值"+re.wt+"存在并列最高"+topGroupInfos.size()+"个时，系统随机在并列组时选择其中一组作为最终派单组；";
			}else {
				GroupInfo group0=orderGroupInfos.get(0),group1=orderGroupInfos.get(1);
				if(group0.wt-group1.wt>1) {
					
					re=orderGroupInfos.get(0);
					msg="当分值最高值不存在并列，且分数最高值"+group0.wt+" 比次高值"+group1.wt+" 高1分及以上的，选择分数最高值组为最终派单组；";
				}else {
					
					int rn=random.nextInt(10);
					re=rn<7?orderGroupInfos.get(0):orderGroupInfos.get(1);
					msg="当分值最高值不存在并列，且分数最高值"+group0.wt+"比次高值"+group1.wt+"差在1分以内的，最高值和次高值按7：3的概率随机选出随机数"+rn+"最终派单组；";
				}
			}
            System.out.println("抽签结果:"+msg);
			return re;
		}
		
		
		protected static void print(List<GroupInfo> groupInfos) {
			System.out.println("用户ID 	 	规则1  	规则2  	规则3  	规则4  	权重  	  概率 	  	规则3具体数据    规则4具体数据");
			for (GroupInfo groupInfo : groupInfos) {
				//groupInfoMap.put(groupInfo.id, groupInfo);
				String msg = MessageFormat.format("id:{0}  	 {1}  	 {2}  	 {3}  	 {4}    	{5}  	{6}    	 {7}   		{8}", groupInfo.id,
						groupInfo.priority1, groupInfo.priority2, groupInfo.priority3, groupInfo.priority4,
						groupInfo.wt, groupInfo.wtRate, groupInfo.toDoTaskCount, groupInfo.totalTaskCount);
				System.out.println(msg);
			}
		}
	}

	// 人员类
	protected static class Employee {

		String id, portCode;

		Set<String> skillIds = new HashSet<>();

		
		List<TaskInfo> doneTasks=new ArrayList<>()
				,toDoTasks=new ArrayList<>()
				,doingTasks=new ArrayList<>()
				,tempTasks=new ArrayList<>()
				;

		public Employee(String id, String... skillIds) {
			super();
			this.id = id;
			for (String skillId : skillIds) {
				this.skillIds.add(skillId);
			}
		}
		
		int totalTaskCount() {
			return doneTasks.size()+toDoTasks.size()+doingTasks.size();
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
		
		
		
		boolean canDoTask(TaskInfo taskInfo) {
			boolean re=false;
			String remark="办理正常";
			
			
			if(this.tempTasks.size()<=2) {
				re=true;
			}else  {
				Set<String> donePortTop3Set=new HashSet<>();
		    	for(int i=0;i<3;i++) {
		    		donePortTop3Set.add(tempTasks.get(i).portCode);
		    	}
		    	
				
				if(donePortTop3Set.size()==1 ) {
					//如果前三个码头相同
					String samePort=donePortTop3Set.iterator().next();
					if(tempTasks.size()>5) {
						re=true;
						remark="我在同一个码头 "+samePort+" 已经达到5次，不能再去任何码头";
					}else if(taskInfo.portCode.equals(samePort)) {
					    //这时只能再办一次同一码头的
					    re=true;
					}else {
						re=false;
						remark="我在同一个码头 "+samePort+" 已经超过3次，不能再去其他码头："+taskInfo.portCode;
					}
					
				}
				//else if(PortCodeEnum.PORT_CODE_0.value.equals(taskInfo.portCode)) {
				//	re=true;
				//	remark="我在跨码头 已经超过2次，：但我可以去任意码头:"+taskInfo.portCode;
				//}
				else {
					remark="我在跨码头 已经达到3次，不能再接码头："+taskInfo.portCode;
				}
				
		    }
			
			
			
		    taskInfo.remark="人员:"+id+"  "+remark;
			
			
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
					 //ex.printStackTrace();
				}
			}

			return re;
		}

		
        protected static void print(Collection<Employee> employees) {
        	System.out.println("用户ID 	 待办                  在办  	  已办  	合计   最近去过的码头  总共 去过的码头");
			for (Employee emp : employees) {
				String ports="",tempports="";
				for(TaskInfo ti:emp.doneTasks) {
					ports+=ti.portCode+",";
				}
				for(TaskInfo ti:emp.tempTasks) {
					tempports+=ti.portCode+",";
				}
				String msg = MessageFormat.format(
						"id:{0}  	 {1}  	 {2}  	 {3}  	 {4}    {5}     {6}", emp.id,
						emp.toDoTasks.size(), emp.doingTasks.size(),
						emp.doneTasks.size(), 
						
						emp.totalTaskCount()
						,tempports
						,ports
						);
				System.out.println(msg);
			}
        }
		
		
	}

	// 算法入口
	private static Map<String, Employee[]> calEmployeeGroupBySkill(Collection<Employee> allEmployees,
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

					if (addEmployeeGroupMap.containsKey(groupGtiId)&&Employee.isContain(addEmployeeGroupMap.get(groupGtiId),
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

	private static List<GroupInfo> calGroupInfo(Collection<Employee> allEmployees, String portCode, String... needSkillIds) {
		List<GroupInfo> groupInfos = new ArrayList<>();

		Map<String, Employee[]> employeeGroupMap = calEmployeeGroupBySkill(allEmployees, needSkillIds);

		for (String key : employeeGroupMap.keySet()) {
			GroupInfo groupInfo = new GroupInfo(key, employeeGroupMap.get(key));
			groupInfos.add(groupInfo);
		}
		GroupInfo.updatePriority3(groupInfos);
		GroupInfo.updatePriority4(groupInfos);
		for (int i = 0; i < groupInfos.size(); i++) {
			GroupInfo groupInfo = groupInfos.get(i);
			groupInfo.updatePriority1();
			groupInfo.updatePriority2(portCode);
			
			groupInfo.updateWT(groupInfos);
		}

		
		for (int i = 0; i < groupInfos.size(); i++) {
			GroupInfo groupInfo = groupInfos.get(i);

			groupInfo.updateWTRate(groupInfos);
		}
		return groupInfos;
	}

	private static Map<String,Employee> loadEmployees() {

		List<Employee> employees = new ArrayList<>();

		employees.add(new Employee("1", "A", "C"));
		employees.add(new Employee("2", "A", "B", "D"));
		employees.add(new Employee("3", "E"));
		employees.add(new Employee("4", "A", "C", "D", "E"));
		employees.add(new Employee("5", "B"));
		employees.add(new Employee("6", "D"));
		employees.add(new Employee("7", "C"));
		employees.add(new Employee("8", "B", "D", "E"));

		
		Map<String,Employee> employessMap=toMap(employees);
		return employessMap;

	}
	
	public enum SkillsEnum{
		SKILL_A(1,"A"),
		SKILL_B(2,"B"),
		SKILL_C(3,"C"),
		SKILL_D(4,"D"),
		SKILL_E(5,"E");
		
		private int num;
		
		private String value;
		
		private SkillsEnum(int num, String value) {
			this.num = num;
			this.value = value;
		}
		
		public static String getValueByNum(int num){
			for(SkillsEnum skill:SkillsEnum.values() ){
				if(skill.num==num){
					return skill.value;
				}
			}
			return null;
		}
		
		
		public static String[] random(int min){
			//随机产生技能个数
			int max=SkillsEnum.values().length;
			int[] skillTotalNum = randomArray(min, max,1);
			
			
			//具体的技能编号
			int[] skillDetail = randomArray(min, max, skillTotalNum[0]);
			ArrayList<String> list = new ArrayList<String>();
			for (int skillNum : skillDetail) {
				String skill = SkillsEnum.getValueByNum(skillNum);
				list.add(skill);
				
			}
			return (String[]) list.toArray(new String[list.size()]);
			
		}
		
		
	}
	public enum PortCodeEnum{
		PORT_CODE_A(1,"A"),
		PORT_CODE_B(2,"B"),
		PORT_CODE_C(3,"C"),
		PORT_CODE_D(4,"D"),
		PORT_CODE_E(5,"E"),
		PORT_CODE_F(6,"F"),
		PORT_CODE_G(7,"G");
		
		private int num;
		
		private String value;
		
		private PortCodeEnum(int num, String value) {
			this.num = num;
			this.value = value;
		}
		
		public static String getValueByNum(int num){
			for(PortCodeEnum skill:PortCodeEnum.values() ){
				if(skill.num==num){
					return skill.value;
				}
			}
			return null;
		}
		
		
		public static String random(int min){
			//随机生成码头年编号
			int[] portCodeNum = randomArray(min, PortCodeEnum.values().length,1);
			
			return PortCodeEnum.getValueByNum(portCodeNum[0]);
			
		}
		
	}

	public static void main(String[] args) {

		//List<Employee> employees = loadEmployees();
		Map<String,Employee> employessMap=loadEmployees();
		
		
		List<TaskInfo> historyTaskInfos=new ArrayList<>();
	   //List<GroupInfo> historyGroupInfo=new ArrayList<GroupInfo>();
		
		for (int i = 0; i < 200; i++) {
			String port = PortCodeEnum.random(1);
			String[] needSkills = SkillsEnum.random(1);
			
			while(needSkills.length<2) {
				needSkills = SkillsEnum.random(1);
			}
			
			System.out.println("\n\n"+i+" 申请任务   监管点:" + port + "  技能:" + String.join(",", needSkills));
			
			
			
			
			//将选中的信息保存到集合中
			//historyGroupInfo.add(randGroupInfo);
			TaskInfo htaskInfo=new TaskInfo(i,port,needSkills);
			historyTaskInfos.add(htaskInfo);
			
			//2  增加X次（可设定，X>200）任务，任务所需要的技能为2到5个技能的随机，码头也随机，每次派单增加的任务都计入个人的待办。
			
			
			List<GroupInfo> groupInfos = calGroupInfo(employessMap.values(), htaskInfo.portCode, htaskInfo.skills);
			Map<String, GroupInfo> groupInfoMap = toMap(groupInfos);
			
			GroupInfo.print(groupInfos);

			htaskInfo.groupInfo = GroupInfo.draw(groupInfoMap);
			System.out.println("抽中：" + htaskInfo.groupInfo);
			
			for (Employee emp : htaskInfo.groupInfo.employees) {
				
				   emp.toDoTasks.add(htaskInfo);
			    
			}
			
			//
//			3.当个位数为5的派单（第5次、15次、25次。。。。）执行完毕后，对所有待办任务批量执行一次“待办转在办”的操作，具体规则为：
//			从派单序号从小到大运算，当派单执行人员组合中每个人的在办数小于等于5时，可以将这个待办任务接单；当一个组合中任意一人的在办数大于5时，这个任务不接单，待下一回合批量执行“待办转在办”的操作时再优先运算操作。
//			“待办转在办”运算后，显示这一组运算情况（哪几次的派单任务由待办转在办）和运算后每个人的待办、在办、已办、合计
			
			if((i+1)%10==5){
				for (TaskInfo taskInfo:historyTaskInfos) {
				  //已经接过的单就可以省略了
				  if(taskInfo.isAsigned)continue;
				  
				  
				  
				  
				  GroupInfo groupInfo=taskInfo.groupInfo;
				  String msg="接单成功    任务序号:"+taskInfo.seqNo+" 接单组:"+groupInfo.id;
				  
				  boolean isAssign=true;
				  for (Employee groupEmp : groupInfo.employees) {
					   
					    //for(Employee emp:employees) {
					     // if(!emp.id.equals(groupEmp.id))continue;
					      //待办转
					      Employee emp=employessMap.get(groupEmp.id);
					      if(emp.doingTasks.size()>5) {
					    	  //当一个组合中任意一人的在办数大于5时,该任务不接单
					    	  isAssign=false;
					    	  msg="接单拒绝   任务序号:"+taskInfo.seqNo+" 接单组:"+groupInfo.id+"  因为人员 "+emp.id+" 很忙 ,在办数:"+emp.doingTasks.size();
					    	  break;
					      }
					   
				  }
				  taskInfo.isAsigned=isAssign;
				  
				    
				    if(taskInfo.isAsigned) {
				    	for(Employee emp:groupInfo.employees) {
				    		 //Employee emp=employessMap.get(groupEmp.id);
						      //emp.updateCur(0,emp.doingTaskCount+emp.toDoTaskCount, emp.doneTaskCount, emp.portCode);
						     // emp.updateCur(emp.toDoTaskCount-1, emp.doingTaskCount+1, emp.doneTaskCount, emp.portCode);
						     emp.doingTasks.add(taskInfo); 
				    		 emp.toDoTasks.remove(taskInfo);
						 }
				    }
				  
				  System.out.println(msg);
				}
				
				
				System.out.println("=========待办转在办============");
				Employee.print(employessMap.values());
				
			}
			
			//当个位数为0的派单（第10次、20次、30次
			if((i+1)!=0 &&  (i+1)%10==0){
				//清空所有人员的临时任务
				for(Employee emp:employessMap.values()) {
					  
					  emp.tempTasks.clear();
				  }
				
				for (TaskInfo taskInfo:historyTaskInfos) {
					  //未接的单或已经完成的单就可以省略了
					  if(!taskInfo.isAsigned||taskInfo.isDone)continue;
					  
					  
					  
					  GroupInfo groupInfo=taskInfo.groupInfo;
					  String msg="办理完成    任务序号:"+taskInfo.seqNo+" 接单组:"+groupInfo.id;
					  boolean isDo=true;
					  for (Employee emp : groupInfo.employees) {
						    
						    ////for(Employee emp:employees) {
						    //  if(!emp.id.equals(groupEmp.id))continue;
						      //待办转
						     //Employee emp=employessMap.get(groupEmp.id); 
						      
						      if(!emp.canDoTask(taskInfo)) {
						    	  isDo=false;
						    	  msg="任务拒办   任务序号:"+taskInfo.seqNo+" 接单组:"+groupInfo.id ;
						    	  msg+="\n"+taskInfo.remark;
						    	  break;
						      }
						      
						   // }
						    
						    
						   
					  }
					  taskInfo.isDone=isDo;
					  
					  if(taskInfo.isDone) {
					    	for(Employee emp:groupInfo.employees) {
					    		//Employee emp=employessMap.get(groupEmp.id); 
							      
							      //emp.updateCur(emp.toDoTaskCount, emp.doingTaskCount-1, emp.doneTaskCount+1, taskInfo.portCode);
					    		  emp.doingTasks.remove(taskInfo);
							      emp.doneTasks.add(taskInfo);
							      emp.tempTasks.add(taskInfo);
							      emp.portCode=taskInfo.portCode;
							 }
					    }
					  
					  
					  System.out.println(msg);
					}
					
					
					System.out.println("=========待办转在办============");
					Employee.print(employessMap.values());
			}
			
		}

	}
	
	
	
	
//	2.增加X次（可设定，X>200）任务，任务所需要的技能为2到5个技能的随机，码头也随机，每次派单增加的任务都计入个人的待办。
//	每次任务派完后，都显示派单情况和派单后每个人的待办、在办、已办、合计
	
	
	
	/**
	 * @param min	随机数所在范围 最小
	 * @param max	随机数所在范围 最大
	 * @param n:随机数的个数
	 * @return
	 */
	public static int[] randomArray(int min, int max, int n) {
		int len = max - min + 1;

		if (max < min || n > len) {
			return null;
		}

		// 初始化给定范围的待选数组
		int[] source = new int[len];
		for (int i = min; i < min + len; i++) {
			source[i - min] = i;
		}

		int[] result = new int[n];
		Random rd = new Random();
		for (int i = 0; i < result.length; i++) {
			int index = rd.nextInt(source.length);
			// 将随机到的数放入结果集
			result[i] = source[index];
			// 将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
			source[index] = source[source.length-1];
			source=Arrays.copyOf(source, source.length-1);
		}
		return result;
	}

	
	public static <T> Map<String,T> toMap(Collection<T> ts){
		Map<String,T> tm=new HashMap<>();
		for(T t:ts) {
			tm.put(t.toString(), t);
		}
		return tm;
	}
}
