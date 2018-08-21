package indi.ysgj1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmployeeTaskMain1 {

	//每组最少人数
	private final static int MIN_GROUP_SIZE=2;
	
	
	protected static class GroupInfo{
		
		String id;
		
		Employee[] employees;
		
		
		
		int rule1Priority,rule2Priority,rule3Priority,rule4Priority,wt;



		public GroupInfo(String id, Employee[] employees) {
			super();
			this.id = id;
			this.employees = employees;
		}
		
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.id;
		}
		
		//（1）优先选派组内所有人员都无正在执行的任务的组合；
		//（所有人员都无正在执行的任务的组合*5；其他组合*1）
		public void calRule1Priority() {
			int p=1;
			
			//rule1Priority
		}
		
	}
	
	
	//人员类
	protected static class Employee{
		
		String id,portCode;
		
		Set<String> skillIds=new HashSet<>();
		
		int toDoTaskCount,doingTaskCount,doneTaskCount;

		public Employee(String id, String... skillIds) {
			super();
			this.id = id;
			for(String skillId:skillIds) {
				this.skillIds.add(skillId);	
			}
		}
		
		//是否满足技能
		int hasSkill(String...skillIds) {
			int re=0;
			for(String skillId:skillIds) {
				if(this.skillIds.contains(skillId)) {
					re++;
				}	
			}
			return re;
		}
		
		
		protected static int hasSkill(Employee[] employees,String... skillIds) {
			int re=0;
			for(String skillId:skillIds) {
				for(Employee employee:employees) {
				  if(employee.skillIds.contains(skillId)) {
					 re++;
					 break;
				  }	
				}
			}
			return re;
		}
		
		//按人员id排序生成组id
		protected static String genGroupId(Employee...employees ) {
			String re="";
			String []str =new String[employees.length];
            
			for(int i=0;i<str.length;i++){
				   str[i]=employees[i].toString();
			}
			Arrays.sort(str);
			for(int i=0;i<str.length;i++){
			    re+=str[i]+"|";
			}
			return re;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.id;
		}
		
		protected static Employee[] appendTo(Employee[] employees,Employee...ems) {
			Set<Employee> re=new HashSet<>();
			for(Employee employee:employees) {
				re.add(employee);
			}
			for(Employee employee:ems) {
				re.add(employee);
			}
			return re.toArray(new Employee[re.size()]);
		}
		
		
		protected static int isContain(Employee[] employees,Employee[] subEmployees) {
			int re=0;
			
			for(Employee subEmployee:subEmployees) {
				try {
			      for(Employee employee:employees) {
			    	  
			    	  if(subEmployee==employee) {
			    		  re++;
			    		  break;
			    	  }
			    	  
			      }	
				}catch(Exception ex) {
					//ex.printStackTrace();
				}
			}
			
			return re;
		}
		
		
		protected void updateCur(int toDoTaskCount,int doingTaskCount,int doneTaskCount,String portCode) {
			this.toDoTaskCount=toDoTaskCount;
			this.doingTaskCount=doingTaskCount;
			this.doneTaskCount=doneTaskCount;
			this.portCode=portCode;
		}
		
	}
	
	
	
	
	//算法入口
	private static Map<String, Employee[]> calEmployeeGroupBySkill(List<Employee> allEmployees,String... needSkillIds){
		Map<String, Employee[]> candidateEmployeeGroupMap=new HashMap<>()
				,addEmployeeGroupMap=new HashMap<>()
		;
				;
	   
		//候选组人员
		Set<Employee> candidateEmployees=new HashSet<>();
		for(Employee employee:allEmployees) {
			if(employee.hasSkill(needSkillIds)>0) {
				//至少符合一个需求技能才能加入候选组
				candidateEmployeeGroupMap.put(Employee.genGroupId(employee), new Employee[] {employee});
				candidateEmployees.add(employee);
			}
			
		}
		
		
		for(int i=2;i<=needSkillIds.length;i++) {
			//两人小组开始计算符合要求的组合
			Map<String, Employee[]> calEmployeeGroupMap=_calEmployeeGroupBySkill(candidateEmployees,candidateEmployeeGroupMap, i, needSkillIds);
			addEmployeeGroupMap.putAll(calEmployeeGroupMap);
		}
		
		
		//分级剪纸，类似购物篮的剪枝
        for(int i=2;i<=needSkillIds.length;i++) {
        	Set<String> groupEqiIds=new HashSet<>(),groupGtiIds=new HashSet<>();
			for(String groupId:addEmployeeGroupMap.keySet()) {
				Employee[] addEmployees=addEmployeeGroupMap.get(groupId);
				if(addEmployees.length==i) {
					groupEqiIds.add(groupId);
				}else if(addEmployees.length>i) {
					groupGtiIds.add(groupId);
				}
			}
			
			for(String groupGtiId:groupGtiIds) {
			
				for(String groupEqiId:groupEqiIds) {
					
					if(Employee.isContain(addEmployeeGroupMap.get(groupGtiId), addEmployeeGroupMap.get(groupEqiId))>=i) {
						addEmployeeGroupMap.remove(groupGtiId);
					}
					
				}
				
			}
			
			
		}
		
		return addEmployeeGroupMap;
		
	}
	
	
    private static Map<String, Employee[]> _calEmployeeGroupBySkill(Set<Employee> candidateEmployees,Map<String, Employee[]> candidateEmployeeGroupMap,int groupSize,String... needSkillIds){
    	Map<String, Employee[]> addEmployeeGroupMap=new HashMap<>();
        String[] groupIds=candidateEmployeeGroupMap.keySet().toArray(new String[candidateEmployeeGroupMap.size()] );
    	for(String groupId:groupIds) {
    		Employee[] employees=candidateEmployeeGroupMap.get(groupId);
    		if(employees.length!=groupSize-1)continue;
    		
    		
    			
             for(Employee candidateEmployee:candidateEmployees) {
                	Employee[] newEmpGroup=Employee.appendTo(employees, candidateEmployee);
                	String newGroupId=Employee.genGroupId(newEmpGroup);
                	if(Employee.hasSkill(newEmpGroup, needSkillIds)>=needSkillIds.length && newEmpGroup.length>=MIN_GROUP_SIZE) {
                		//满足所有技能加入执行组
                		addEmployeeGroupMap.put(newGroupId,newEmpGroup);
                	}else {
                		//未满足所有技能加入候选组
                		candidateEmployeeGroupMap.put(newGroupId,newEmpGroup);
                	}
             }
    			
    		
    		
    	}
    	
    	return addEmployeeGroupMap;
	}
	
    
    private static List<GroupInfo> calGroupInfo(List<Employee> allEmployees,String portCode,String... needSkillIds){
    	List<GroupInfo> groupInfos=new ArrayList<>();
    	
    	return groupInfos;
    }
	
	
private static List<Employee> loadEmployees(){
		
		List<Employee> employees=new ArrayList<>();
		
		employees.add(new Employee("1", "A","C"));
		employees.add(new Employee("2", "A","B","D"));
		employees.add(new Employee("3", "E"));
		employees.add(new Employee("4", "A","C","D","E"));
		employees.add(new Employee("5", "B"));
		employees.add(new Employee("6", "D"));
		employees.add(new Employee("7", "C"));
		employees.add(new Employee("8", "B","D","E"));
		
		
		employees.get(0).updateCur(4, 0, 2, "B");
		employees.get(1).updateCur(3, 1, 3, "B");
		employees.get(2).updateCur(2, 2, 4, "A");
		employees.get(3).updateCur(2, 0, 2, "A");
		employees.get(4).updateCur(1, 1, 3, "B");
		employees.get(5).updateCur(0, 2, 4, "B");
		employees.get(6).updateCur(4, 1, 3, "B");
		employees.get(7).updateCur(0, 0, 2, null);
		
		
		
		return employees;
		
	}
    
	public static void main(String[] args) {
		
		List<Employee> employees=loadEmployees();
		Map<String, Employee[]> employeeGroupMap= calEmployeeGroupBySkill(employees,"A","C","E");
		
		for(String groupId: employeeGroupMap.keySet()) {
		    Employee[] groupEmps=employeeGroupMap.get(groupId);
		    //if(groupEmps.length>2)continue;
			System.out.println(groupId);
		}
	}
	
	
	
	
	
	
	
}
