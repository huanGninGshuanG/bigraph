package org.bigraph.bigsim.utils

object OS{

  val schedule="""
      |%active Process : 2;
      |%active Stack : 0;
      |%active ProcessStatus : 3;
      |%active ReadyList : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active DataSection : 2;
      |%active ProgramSection : 2;
      |%active PCB : 2;
      |%active LIdx : 3;
      |%active RIdx : 3;
      |%active QueueIdx : 3;
      |%active PID : 2;
      |%active Function : 2;
      |%active Destoryed : 0;
      |
      |# Rules
      |%rule r_create Process[idle,idle] | Num1[idle] | Num0[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[a:edge,b:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[c:edge] | Num0[idle] | Plus[b:edge,a:edge,c:edge]{};
      |
      |%rule r_enter Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | $0 | $1) | ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,a:edge,idle] | $2 | Process[idle,idle].($1 | $0 | PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[e:edge,idle,idle])) | Num0[d:edge] | Num1[idle] | Plus[a:edge,e:edge,d:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[idle,idle,idle] | RIdx[c:edge,b:edge,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[a:edge] | Plus[b:edge,c:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | $2) | Num0[idle] | Num1[a:edge] | Process[idle,idle].($0 | $1 | PCB[idle,idle].(PID[idle,idle] | ProcessStatus[c:edge,b:edge,idle])) | Plus[a:edge,c:edge,b:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_run Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[b:edge,c:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[c:edge,a:edge,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[b:edge] | Plus[a:edge,c:edge,b:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx};
      |
      |%rule r_destory P1:Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | P2:Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) -> P1:Process[idle,idle].Destoryed | P2:Process[idle,idle].Destoryed{Condition:P1.PCB.ProcessStatus==3,P2.PCB.ProcessStatus==3};
      |
      |
      |
      |# prop
      |%prop p  Q:ReadyList.(L:LIdx[idle,idle,idle] | R:RIdx[idle,idle,idle]) | P1:Process[idle,idle].Destoryed | P2:Process[idle,idle].Destoryed{};
      |
      |
      |# Model
      |%agent  Q:ReadyList.(L:LIdx<0>[idle,idle,idle] | R:RIdx<0>[idle,idle,idle]) | N0:Num0<0>[idle] | N1:Num1<1>[idle] | P1:Process[idle,idle] | P2:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

  val scheduleCreate=
"""
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active Stack : 0;
      |%active ProcessStatus : 3;
      |%active ReadyList : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active DataSection : 2;
      |%active ProgramSection : 2;
      |%active PCB : 2;
      |%active LIdx : 3;
      |%active RIdx : 3;
      |%active QueueIdx : 3;
      |%active PID : 2;
      |%active Function : 2;
      |
      |# Rules
      |%rule r_create Process[idle,idle] | Num1[idle] | Num0[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[a:edge,b:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[c:edge] | Num0[idle] | Plus[b:edge,a:edge,c:edge]{};
      |
      |%rule r_enter Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | $0 | $1) | ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,a:edge,idle] | $2 | Process[idle,idle].($1 | $0 | PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[e:edge,idle,idle])) | Num0[d:edge] | Num1[idle] | Plus[a:edge,e:edge,d:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[idle,idle,idle] | RIdx[c:edge,b:edge,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[a:edge] | Plus[b:edge,c:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[c:edge,a:edge,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[b:edge] | Plus[a:edge,c:edge,b:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | PID[idle,idle]) | QueueIdx[idle,idle,idle] | $1 | $0) | $2) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | $2) | Num0[idle] | Num1[a:edge] | Process[idle,idle].($0 | $1 | PCB[idle,idle].(PID[idle,idle] | ProcessStatus[c:edge,b:edge,idle])) | Plus[a:edge,c:edge,b:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_run Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[b:edge,c:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_destory Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) -> Process[idle,idle].Process[idle,idle]{Condition:Process.PCB.ProcessStatus==3};
      |
      |
      |
      |# prop
      |%prop p Q:ReadyList.(L:LIdx[idle,idle,idle] | R:RIdx[idle,idle,idle]) | P1:Process[idle,idle].Destoryed | P2:Process[idle,idle].Destoryed{};
      |
      |
      |# Model
      |%agent  Q:ReadyList.(L:LIdx<0>[idle,idle,idle] | R:RIdx<0>[idle,idle,idle]) | N0:Num0<0>[idle] | N1:Num1<1>[idle] | P2:Process[idle,idle] | P1:Process[idle,idle];
      |
      |
      |
      |
      |
      |# CTL_Formula
      |%ctlSpec EF(p);
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin
val rwTest=
"""
    |# Controls
    |%active Greater : 2;
    |%active Less : 2;
    |%active GreaterOrEqual : 2;
    |%active LessOrEqual : 2;
    |%active Equal : 2;
    |%active NotEqual : 2;
    |%active Exist : 1;
    |%active InstanceOf : 2;
    |%active Plus : 3;
    |%active Minus : 3;
    |%active Multiply : 3;
    |%active Division : 3;
    |%active Opposite : 2;
    |%active Abs : 2;
    |%active Process : 2;
    |%active INT : 2;
    |%active List : 1;
    |%active Function : 2;
    |%active OperatingSystem : 2;
    |%active Status : 0;
    |%active Trap : 2;
    |%active Struct : 2;
    |%active String : 2;
    |%active File : 2;
    |%active VFS : 2;
    |%active Pointer : 0;
    |%active equal : 2;
    |%active ProcessStatus : 2;
    |%active ReadyList : 0;
    |%active DataSection : 0;
    |%active PogramSection : 0;
    |%active PCB : 0;
    |%active PID : 0;
    |%active Num1 : 1;
    |%active Num0 : 1;
    |%active ReadRequest : 1;
    |%active WriteOperation : 1;
    |%active LIdx : 2;
    |%active RIdx : 2;
    |%active QueueIdx : 2;
    |%active WriteRequest : 0;
    |
    |# Rules
    |%rule r_0 Process[idle,idle] | Num1[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[c:edge,b:edge]) | PogramSection.Function[idle,idle] | DataSection) | Num1[a:edge] | Plus[b:edge,c:edge,a:edge]{};
    |
    |%rule r_1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
    |
    |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
    |
    |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
    |
    |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
    |
    |%rule r_2 read_write:Process[d:edge,idle].readf:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle]{};
    |
    |%rule r_3 linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
    |
    |%rule r_4 read_write:Process[d:edge,idle].writef:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle]{};
    |
    |%rule r_5 linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
    |
    |%rule r_6 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_7 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_8 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_9 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
    |
    |%rule r_10 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,idle].filename:String[idle,idle]){};
    |
    |%rule r_11 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |%rule r_12 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_13 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_14 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_15 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
    |
    |%rule r_16 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
    |
    |%rule r_17 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,idle].filename:String[idle,idle]){};
    |
    |%rule r_18 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
    |
    |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
    |
    |%rule r_19 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_20 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_21 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].(nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | pag:File[idle,idle])){};
    |
    |%rule r_22 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_23 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
    |
    |%rule r_24 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_25 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |%rule r_26 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]){};
    |
    |%rule r_27 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_28 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_29 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]){};
    |
    |%rule r_30 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_31 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
    |
    |%rule r_32 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_33 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |%rule r_34 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | pdflush:Process[idle,a:edge]){};
    |
    |%rule r_35 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle]{};
    |
    |%rule r_36 linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle] -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle]{};
    |
    |%rule r_37 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | pdflush:Process[idle,a:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
    |
    |%rule r_38 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(page_dirty:File[a:edge,b:edge] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle] -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(page:File[idle,idle] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
    |
    |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
    |
    |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
    |
    |
    |
    |
    |
    |# Model
    |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle];
    |
    |
    |
    |
    |
    |
    |
    |
    |#SortingLogic
    |
    |
    |# Go!
    |%check;
    |
    |""".stripMargin

  val FCFS2=
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active Stack : 0;
      |%active ProcessStatus : 3;
      |%active ReadyList : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active DataSection : 2;
      |%active ProgramSection : 2;
      |%active PCB : 2;
      |%active LIdx : 3;
      |%active RIdx : 3;
      |%active QueueIdx : 3;
      |%active PID : 2;
      |%active Function : 2;
      |%active Destoryed : 0;
      |
      |# Rules
      |%rule r_create Process[idle,idle] | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[c:edge,b:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[a:edge] | Plus[b:edge,c:edge,a:edge]{};
      |
      |%rule r_destory Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) -> Process[idle,idle].Destoryed{Condition:Process.PCB.ProcessStatus==3};
      |
      |%rule r_enter ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | $3) | Num0[idle] | Num1[idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | $0) | $1 | $2) -> ReadyList.(LIdx[idle,idle,idle] | RIdx[e:edge,b:edge,idle] | $3 | Process[idle,idle].($2 | $1 | PCB[idle,idle].($0 | ProcessStatus[idle,idle,idle]) | QueueIdx[c:edge,idle,idle])) | Num0[a:edge] | Num1[d:edge] | Plus[c:edge,e:edge,d:edge] | Plus[b:edge,c:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | $1) | QueueIdx[idle,idle,idle] | $2 | $3) | $0) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[e:edge,d:edge,idle] | RIdx[idle,idle,idle] | $0) | Num0[idle] | Num1[c:edge] | Process[idle,idle].($3 | $2 | PCB[idle,idle].($1 | ProcessStatus[b:edge,a:edge,idle])) | Plus[d:edge,e:edge,c:edge] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx};
      |
      |%rule r_run Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[b:edge,a:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[c:edge] | Plus[a:edge,b:edge,c:edge]{Condition:Process.PCB.ProcessStatus==2};
      |
      |
      |
      |# prop
      |%prop p  Q:ReadyList.(L:LIdx<2>[idle,idle,idle] | R:RIdx<2>[idle,idle,idle]) | P1:Process[idle,idle].Destoryed | P2:Process[idle,idle].Destoryed{};
      |
      |
      |# Model
      |%agent  Q:ReadyList.(L:LIdx<0>[idle,idle,idle] | R:RIdx<0>[idle,idle,idle]) | N0:Num0<0>[idle] | N1:Num1<1>[idle] | P1:Process[idle,idle] | P2:Process[idle,idle];
      |
      |
      |
      |
      |
      |# CTL_Formula
      |%ctlSpec EF(p);
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin
  val FCFS3=
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active Stack : 0;
      |%active ProcessStatus : 3;
      |%active ReadyList : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active DataSection : 2;
      |%active ProgramSection : 2;
      |%active PCB : 2;
      |%active LIdx : 3;
      |%active RIdx : 3;
      |%active QueueIdx : 3;
      |%active PID : 2;
      |%active Function : 2;
      |%active Destoryed : 0;
      |
      |# Rules
      |%rule r_create Process[idle,idle] | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[c:edge,b:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[a:edge] | Plus[b:edge,c:edge,a:edge]{};
      |
      |%rule r_destory Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) -> Process[idle,idle].Destoryed{Condition:Process.PCB.ProcessStatus==3};
      |
      |%rule r_enter ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | $3) | Num0[idle] | Num1[idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | $0) | $1 | $2) -> ReadyList.(LIdx[idle,idle,idle] | RIdx[e:edge,b:edge,idle] | $3 | Process[idle,idle].($2 | $1 | PCB[idle,idle].($0 | ProcessStatus[idle,idle,idle]) | QueueIdx[c:edge,idle,idle])) | Num0[a:edge] | Num1[d:edge] | Plus[c:edge,e:edge,d:edge] | Plus[b:edge,c:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | $1) | QueueIdx[idle,idle,idle] | $2 | $3) | $0) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[e:edge,d:edge,idle] | RIdx[idle,idle,idle] | $0) | Num0[idle] | Num1[c:edge] | Process[idle,idle].($3 | $2 | PCB[idle,idle].($1 | ProcessStatus[b:edge,a:edge,idle])) | Plus[d:edge,e:edge,c:edge] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx};
      |
      |%rule r_run Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[b:edge,a:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[c:edge] | Plus[a:edge,b:edge,c:edge]{Condition:Process.PCB.ProcessStatus==2};
      |
      |
      |
      |# prop
      |%prop p  Q:ReadyList.(L:LIdx<2>[idle,idle,idle] | R:RIdx<2>[idle,idle,idle]) | P1:Process[idle,idle].Destoryed | P2:Process[idle,idle].Destoryed{};
      |
      |
      |# Model
      |%agent  Q:ReadyList.(L:LIdx<0>[idle,idle,idle] | R:RIdx<0>[idle,idle,idle]) | N0:Num0<0>[idle] | N1:Num1<1>[idle] | P1:Process[idle,idle] | P2:Process[idle,idle] | P3:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

  val FCFS4=
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active Stack : 0;
      |%active ProcessStatus : 3;
      |%active ReadyList : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active DataSection : 2;
      |%active ProgramSection : 2;
      |%active PCB : 2;
      |%active LIdx : 3;
      |%active RIdx : 3;
      |%active QueueIdx : 3;
      |%active PID : 2;
      |%active Function : 2;
      |%active Destoryed : 0;
      |
      |# Rules
      |%rule r_create Process[idle,idle] | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[c:edge,b:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[a:edge] | Plus[b:edge,c:edge,a:edge]{};
      |
      |%rule r_destory Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) -> Process[idle,idle].Destoryed{Condition:Process.PCB.ProcessStatus==3};
      |
      |%rule r_enter ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | $3) | Num0[idle] | Num1[idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | $0) | $1 | $2) -> ReadyList.(LIdx[idle,idle,idle] | RIdx[e:edge,b:edge,idle] | $3 | Process[idle,idle].($2 | $1 | PCB[idle,idle].($0 | ProcessStatus[idle,idle,idle]) | QueueIdx[c:edge,idle,idle])) | Num0[a:edge] | Num1[d:edge] | Plus[c:edge,e:edge,d:edge] | Plus[b:edge,c:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle,idle] | RIdx[idle,idle,idle] | Process[idle,idle].(PCB[idle,idle].(ProcessStatus[idle,idle,idle] | $1) | QueueIdx[idle,idle,idle] | $2 | $3) | $0) | Num0[idle] | Num1[idle] -> ReadyList.(LIdx[e:edge,d:edge,idle] | RIdx[idle,idle,idle] | $0) | Num0[idle] | Num1[c:edge] | Process[idle,idle].($3 | $2 | PCB[idle,idle].($1 | ProcessStatus[b:edge,a:edge,idle])) | Plus[d:edge,e:edge,c:edge] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx};
      |
      |%rule r_run Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[idle,idle,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[idle] -> Process[idle,idle].(PCB[idle,idle].(PID[idle,idle] | ProcessStatus[b:edge,a:edge,idle]) | ProgramSection[idle,idle].Function[idle,idle] | DataSection[idle,idle]) | Num1[c:edge] | Plus[a:edge,b:edge,c:edge]{Condition:Process.PCB.ProcessStatus==2};
      |
      |
      |
      |# prop
      |%prop p  Q:ReadyList.(L:LIdx<2>[idle,idle,idle] | R:RIdx<2>[idle,idle,idle]) | P1:Process[idle,idle].Destoryed | P2:Process[idle,idle].Destoryed{};
      |
      |
      |# Model
      |%agent  Q:ReadyList.(L:LIdx<0>[idle,idle,idle] | R:RIdx<0>[idle,idle,idle]) | N0:Num0<0>[idle] | N1:Num1<1>[idle] | P1:Process[idle,idle] | P2:Process[idle,idle] | P3:Process[idle,idle] | P4:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

  val rw2Process=
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active INT : 2;
      |%active List : 1;
      |%active Function : 2;
      |%active OperatingSystem : 2;
      |%active Status : 0;
      |%active Trap : 2;
      |%active Struct : 2;
      |%active String : 2;
      |%active File : 2;
      |%active VFS : 2;
      |%active Pointer : 0;
      |%active equal : 2;
      |%active ProcessStatus : 2;
      |%active ReadyList : 0;
      |%active DataSection : 0;
      |%active PogramSection : 0;
      |%active PCB : 0;
      |%active PID : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active ReadRequest : 1;
      |%active WriteOperation : 1;
      |%active LIdx : 2;
      |%active RIdx : 2;
      |%active QueueIdx : 2;
      |%active WriteRequest : 0;
      |
      |# Rules
      |%rule r_page1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_page2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_cacheread3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].(nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | pag:File[idle,idle])){};
      |
      |%rule r_cacheread4 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_cacheread5 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
      |
      |%rule r_cacheread6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_cacheread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_cacheread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]){};
      |
      |%rule r_0 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]){};
      |
      |%rule r_3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_4 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
      |
      |%rule r_5 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | pdflush:Process[idle,a:edge]){};
      |
      |%rule r_8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle]{};
      |
      |%rule r_9 linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle] -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle]{};
      |
      |%rule r_10 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | pdflush:Process[idle,a:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
      |
      |%rule r_11 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(page_dirty:File[a:edge,b:edge] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle] -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(page:File[idle,idle] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
      |
      |%rule r_enter1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
      |
      |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
      |
      |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_vfsRead1 read_write:Process[d:edge,idle].readf:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle]{};
      |
      |%rule r_vfsRead2 linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
      |
      |%rule r_12 read_write:Process[d:edge,idle].writef:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle]{};
      |
      |%rule r_13 linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
      |
      |%rule r_14 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_15 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_16 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_17 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_18 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,idle].filename:String[idle,idle]){};
      |
      |%rule r_19 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_vfsRead3 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_vfsRead4 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_vfsRead5 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_20 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_vfsread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_vfsread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,idle].filename:String[idle,idle]){};
      |
      |%rule r_vfsread9 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_21 Process[idle,idle] | Num1[idle] -> Process[idle,idle].PCB.PID | Num1[idle]{};
      |
      |%rule r_22 Process[idle,idle].PCB.(ProcessStatus[idle,idle] | PID) | Num1[idle] -> Process[idle,idle].(PCB.(ProcessStatus[b:edge,a:edge] | PID) | PogramSection.Function[idle,idle] | DataSection) | Num1[c:edge] | Plus[a:edge,b:edge,c:edge]{};
      |
      |%rule r_23 Process[idle,idle].PCB.PID | Num1[idle] -> Process[idle,idle].PCB.(PID | ProcessStatus[idle,idle]) | Num1[idle]{};
      |
      |
      |
      |
      |
      |# Model
      |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin
  //检测EFP要注意原子命题中匿名节点带value的情况
  val rwWithEFP=
"""
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active INT : 2;
      |%active List : 1;
      |%active Function : 2;
      |%active OperatingSystem : 2;
      |%active Status : 0;
      |%active Trap : 2;
      |%active Struct : 2;
      |%active String : 2;
      |%active File : 2;
      |%active VFS : 2;
      |%active Pointer : 0;
      |%active equal : 2;
      |%active ProcessStatus : 2;
      |%active ReadyList : 0;
      |%active DataSection : 0;
      |%active PogramSection : 0;
      |%active PCB : 0;
      |%active PID : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active ReadRequest : 1;
      |%active WriteOperation : 1;
      |%active LIdx : 2;
      |%active RIdx : 2;
      |%active QueueIdx : 2;
      |%active WriteRequest : 0;
      |
      |# Rules
      |%rule r_page1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_page2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_cacheread3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].(nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | pag:File[idle,idle])){};
      |
      |%rule r_cacheread4 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_cacheread5 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
      |
      |%rule r_cacheread6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_cacheread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_cacheread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]){};
      |
      |%rule r_0 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]){};
      |
      |%rule r_3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_4 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
      |
      |%rule r_5 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | pdflush:Process[idle,a:edge]){};
      |
      |%rule r_8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle]{};
      |
      |%rule r_9 linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle] -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle]{};
      |
      |%rule r_10 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | pdflush:Process[idle,a:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
      |
      |%rule r_11 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(page_dirty:File[a:edge,b:edge] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle] -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(page:File[idle,idle] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
      |
      |%rule r_enter1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
      |
      |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
      |
      |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_vfsRead1 read_write:Process[d:edge,idle].readf:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle]{};
      |
      |%rule r_vfsRead2 linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
      |
      |%rule r_12 read_write:Process[d:edge,idle].writef:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle]{};
      |
      |%rule r_13 linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
      |
      |%rule r_14 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_15 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_16 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_17 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_18 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,idle].filename:String[idle,idle]){};
      |
      |%rule r_19 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_vfsRead3 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_vfsRead4 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_vfsRead5 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_20 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_vfsread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_vfsread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,idle].filename:String[idle,idle]){};
      |
      |%rule r_vfsread9 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_21 Process[idle,idle] | Num1[idle] -> Process[idle,idle].PCB.PID | Num1[idle]{};
      |
      |%rule r_22 Process[idle,idle].PCB.(ProcessStatus[idle,idle] | PID) | Num1[idle] -> Process[idle,idle].(PCB.(ProcessStatus[b:edge,c:edge] | PID) | PogramSection.Function[idle,idle] | DataSection) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==0};
      |
      |%rule r_23 Process[idle,idle].PCB.PID | Num1[idle] -> Process[idle,idle].PCB.(PID | ProcessStatus[idle,idle]) | Num1[idle]{};
      |
      |
      |
      |# prop
      |%prop p  P1:Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){};
      |
      |
      |# Model
      |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle];
      |
      |
      |
      |
      |
      |# CTL_Formula
      |%ctlSpec EF(p);
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

val rw3Process=
"""
    |# Controls
    |%active Greater : 2;
    |%active Less : 2;
    |%active GreaterOrEqual : 2;
    |%active LessOrEqual : 2;
    |%active Equal : 2;
    |%active NotEqual : 2;
    |%active Exist : 1;
    |%active InstanceOf : 2;
    |%active Plus : 3;
    |%active Minus : 3;
    |%active Multiply : 3;
    |%active Division : 3;
    |%active Opposite : 2;
    |%active Abs : 2;
    |%active Process : 2;
    |%active INT : 2;
    |%active List : 1;
    |%active Function : 2;
    |%active OperatingSystem : 2;
    |%active Status : 0;
    |%active Trap : 2;
    |%active Struct : 2;
    |%active String : 2;
    |%active File : 2;
    |%active VFS : 2;
    |%active Pointer : 0;
    |%active equal : 2;
    |%active ProcessStatus : 2;
    |%active ReadyList : 0;
    |%active DataSection : 0;
    |%active PogramSection : 0;
    |%active PCB : 0;
    |%active PID : 0;
    |%active Num1 : 1;
    |%active Num0 : 1;
    |%active ReadRequest : 1;
    |%active WriteOperation : 1;
    |%active LIdx : 2;
    |%active RIdx : 2;
    |%active QueueIdx : 2;
    |%active WriteRequest : 0;
    |
    |# Rules
    |%rule r_page1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_page2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_cacheread3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].(nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | pag:File[idle,idle])){};
    |
    |%rule r_cacheread4 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_cacheread5 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
    |
    |%rule r_cacheread6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_cacheread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |%rule r_cacheread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]){};
    |
    |%rule r_0 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
    |
    |%rule r_2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]){};
    |
    |%rule r_3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_4 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
    |
    |%rule r_5 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
    |
    |%rule r_6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |%rule r_7 Process[idle,idle] | Num1[idle] -> Process[idle,idle].PCB.PID | Num1[idle]{};
    |
    |%rule r_8 Process[idle,idle].PCB.(ProcessStatus[idle,idle] | PID) | Num1[idle] -> Process[idle,idle].(PCB.(ProcessStatus[b:edge,c:edge] | PID) | PogramSection.Function[idle,idle] | DataSection) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==0};
    |
    |%rule r_9 Process[idle,idle].PCB.PID | Num1[idle] -> Process[idle,idle].PCB.(PID | ProcessStatus[idle,idle]) | Num1[idle]{};
    |
    |%rule r_10 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | pdflush:Process[idle,a:edge]){};
    |
    |%rule r_11 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle]{};
    |
    |%rule r_12 linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle] -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle]{};
    |
    |%rule r_13 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | pdflush:Process[idle,a:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
    |
    |%rule r_14 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(page_dirty:File[a:edge,b:edge] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle] -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(page:File[idle,idle] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
    |
    |%rule r_enter1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
    |
    |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
    |
    |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
    |
    |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
    |
    |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
    |
    |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
    |
    |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
    |
    |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
    |
    |%rule r_vfsRead1 read_write:Process[d:edge,idle].readf:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle]{};
    |
    |%rule r_vfsRead2 linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
    |
    |%rule r_15 read_write:Process[d:edge,idle].writef:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle]{};
    |
    |%rule r_16 linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
    |
    |%rule r_17 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_18 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_19 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_20 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
    |
    |%rule r_21 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,idle].filename:String[idle,idle]){};
    |
    |%rule r_22 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |%rule r_vfsRead3 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_vfsRead4 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_vfsRead5 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
    |
    |%rule r_23 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
    |
    |%rule r_vfsread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
    |
    |%rule r_vfsread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,idle].filename:String[idle,idle]){};
    |
    |%rule r_vfsread9 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
    |
    |
    |
    |# prop
    |%prop p  P1:Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){};
    |
    |
    |# Model
    |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle] | P3:Process[idle,idle];
    |
    |
    |
    |
    |
    |
    |
    |
    |#SortingLogic
    |
    |
    |# Go!
    |%check;
    |
    |""".stripMargin


  val rw4Process=
"""
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active INT : 2;
      |%active List : 1;
      |%active Function : 2;
      |%active OperatingSystem : 2;
      |%active Status : 0;
      |%active Trap : 2;
      |%active Struct : 2;
      |%active String : 2;
      |%active File : 2;
      |%active VFS : 2;
      |%active Pointer : 0;
      |%active equal : 2;
      |%active ProcessStatus : 2;
      |%active ReadyList : 0;
      |%active DataSection : 0;
      |%active PogramSection : 0;
      |%active PCB : 0;
      |%active PID : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active ReadRequest : 1;
      |%active WriteOperation : 1;
      |%active LIdx : 2;
      |%active RIdx : 2;
      |%active QueueIdx : 2;
      |%active WriteRequest : 0;
      |
      |# Rules
      |%rule r_page1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_page2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_cacheread3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].(nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | pag:File[idle,idle])){};
      |
      |%rule r_cacheread4 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_cacheread5 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
      |
      |%rule r_cacheread6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_cacheread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_cacheread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumer:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:String[idle,idle] | equal:equal[idle,idle]){};
      |
      |%rule r_0 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_1 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]){};
      |
      |%rule r_2 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[c:edge,b:edge] | null:Struct[idle,idle] | page:File[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]){};
      |
      |%rule r_3 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[b:edge,c:edge] | null:Struct[c:edge,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge] | equal:equal[a:edge,b:edge]) -> linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_4 linux:OperatingSystem[b:edge,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,a:edge] | page:File[idle,idle] | PageFault:Trap[b:edge,a:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]){};
      |
      |%rule r_5 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[a:edge,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[a:edge,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]){};
      |
      |%rule r_6 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle].filename:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle] | filenamePage:File[idle,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle] | page:File[idle,c:edge])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | pdflush:Process[idle,a:edge]){};
      |
      |%rule r_8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[b:edge,idle])) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle].addressspace:INT[idle,idle] | update:Trap[a:edge,b:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle]{};
      |
      |%rule r_9 linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[idle,idle])) | process:Process[c:edge,idle].sync:Function[c:edge,idle] -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge])) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle]{};
      |
      |%rule r_10 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page_dirty:File[a:edge,b:edge]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | pdflush:Process[idle,a:edge]) -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(tree_node:Struct[idle,idle] | null:Struct[idle,idle] | page:File[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
      |
      |%rule r_11 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(DATA:Struct[b:edge,idle].fileUpdate:File[idle,idle] | radix_tree:Struct[idle,idle].(page_dirty:File[a:edge,b:edge] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]) | sync:Process[idle,a:edge]) | process:Process[idle,idle].syncf:Function[idle,idle] -> linux:OperatingSystem[idle,idle].vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | radix_tree:Struct[idle,idle].(page:File[idle,idle] | tree_node:Struct[idle,idle] | null:Struct[idle,idle]) | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(dataAddress:Pointer | atime:Struct[idle,idle] | owner:Struct[idle,idle]) | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle]){};
      |
      |%rule r_enter1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
      |
      |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
      |
      |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_vfsRead1 read_write:Process[d:edge,idle].readf:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle]{};
      |
      |%rule r_vfsRead2 linux:OperatingSystem[idle,idle].vfs_read:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
      |
      |%rule r_12 read_write:Process[d:edge,idle].writef:Function[d:edge,idle] -> linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle]{};
      |
      |%rule r_13 linux:OperatingSystem[idle,idle].vfs_write:Process[idle,idle] -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(DATA:Struct[idle,idle] | sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | address_space:Struct[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer))){};
      |
      |%rule r_14 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_15 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_16 linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_17 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_18 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,idle].filename:String[idle,idle]){};
      |
      |%rule r_19 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_write:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_write:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_vfsRead3 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_vfsRead4 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_vfsRead5 linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,idle].filename:String[idle,idle] | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])) -> linux:OperatingSystem[idle,idle].(vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | x:Status) | vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle])){};
      |
      |%rule r_20 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | r:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_vfsread7 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].(nodeinfo:Struct[idle,idle] | w:Status)) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]){};
      |
      |%rule r_vfsread8 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,idle].filename:String[idle,idle]){};
      |
      |%rule r_vfsread9 linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[a:edge,b:edge] | inodetable:List[idle].inode:Struct[b:edge,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle]) | vfs_read:Process[idle,a:edge].nodeinfo:Struct[idle,idle]) -> linux:OperatingSystem[idle,idle].(vfs:VFS[idle,idle].(sbList:List[idle].superblock:Struct[idle,idle].inodenumber:INT[idle,idle] | inodetable:List[idle].inode:Struct[idle,idle].(owner:Struct[idle,idle] | atime:Struct[idle,idle] | dataAddress:Pointer) | address_space:Struct[idle,idle] | DATA:Struct[idle,idle] | radix_tree:Struct[a:edge,b:edge].(page:File[idle,c:edge] | tree_node:Struct[c:edge,b:edge] | null:Struct[c:edge,idle])) | vfs_read:Process[idle,idle].nodeinfo:Struct[idle,idle].addressspace:INT[idle,a:edge]){};
      |
      |%rule r_21 Process[idle,idle] | Num1[idle] -> Process[idle,idle].PCB.PID | Num1[idle]{};
      |
      |%rule r_22 Process[idle,idle].PCB.(ProcessStatus[idle,idle] | PID) | Num1[idle] -> Process[idle,idle].(PCB.(ProcessStatus[b:edge,c:edge] | PID) | PogramSection.Function[idle,idle] | DataSection) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==0};
      |
      |%rule r_23 Process[idle,idle].PCB.PID | Num1[idle] -> Process[idle,idle].PCB.(PID | ProcessStatus[idle,idle]) | Num1[idle]{};
      |
      |
      |
      |# prop
      |%prop p  P1:Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){};
      |
      |
      |# Model
      |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle] | P3:Process[idle,idle] | P4:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin


  val rw4DEBUG=
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active INT : 2;
      |%active List : 1;
      |%active Function : 2;
      |%active OperatingSystem : 2;
      |%active Status : 0;
      |%active Trap : 2;
      |%active Struct : 2;
      |%active String : 2;
      |%active File : 2;
      |%active VFS : 2;
      |%active Pointer : 0;
      |%active equal : 2;
      |%active ProcessStatus : 2;
      |%active ReadyList : 0;
      |%active DataSection : 0;
      |%active PogramSection : 0;
      |%active PCB : 0;
      |%active PID : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active ReadRequest : 1;
      |%active WriteOperation : 1;
      |%active LIdx : 2;
      |%active RIdx : 2;
      |%active QueueIdx : 2;
      |%active WriteRequest : 0;
      |
      |# Rules
      |%rule r_enter1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
      |
      |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
      |
      |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_0 Process[idle,idle] | Num1[idle] -> Process[idle,idle].PCB.PID | Num1[idle]{};
      |
      |%rule r_1 Process[idle,idle].PCB.(ProcessStatus[idle,idle] | PID) | Num1[idle] -> Process[idle,idle].(PCB.(ProcessStatus[b:edge,c:edge] | PID) | PogramSection.Function[idle,idle] | DataSection) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==0};
      |
      |%rule r_2 Process[idle,idle].PCB.PID | Num1[idle] -> Process[idle,idle].PCB.(PID | ProcessStatus[idle,idle]) | Num1[idle]{};
      |
      |
      |
      |# prop
      |%prop p  P1:Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){};
      |
      |
      |# Model
      |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle] | P3:Process[idle,idle] | P4:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

  val rw3DEBUG=
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active INT : 2;
      |%active List : 1;
      |%active Function : 2;
      |%active OperatingSystem : 2;
      |%active Status : 0;
      |%active Trap : 2;
      |%active Struct : 2;
      |%active String : 2;
      |%active File : 2;
      |%active VFS : 2;
      |%active Pointer : 0;
      |%active equal : 2;
      |%active ProcessStatus : 2;
      |%active ReadyList : 0;
      |%active DataSection : 0;
      |%active PogramSection : 0;
      |%active PCB : 0;
      |%active PID : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active ReadRequest : 1;
      |%active WriteOperation : 1;
      |%active LIdx : 2;
      |%active RIdx : 2;
      |%active QueueIdx : 2;
      |%active WriteRequest : 0;
      |
      |# Rules
      |%rule r_enter1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
      |
      |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
      |
      |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_0 Process[idle,idle] | Num1[idle] -> Process[idle,idle].PCB.PID | Num1[idle]{};
      |
      |%rule r_1 Process[idle,idle].PCB.(ProcessStatus[idle,idle] | PID) | Num1[idle] -> Process[idle,idle].(PCB.(ProcessStatus[b:edge,c:edge] | PID) | PogramSection.Function[idle,idle] | DataSection) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==0};
      |
      |%rule r_2 Process[idle,idle].PCB.PID | Num1[idle] -> Process[idle,idle].PCB.(PID | ProcessStatus[idle,idle]) | Num1[idle]{};
      |
      |
      |
      |# prop
      |%prop p  P1:Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){};
      |
      |
      |# Model
      |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle] | P3:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

  val rw2DEBUG=
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 2;
      |%active INT : 2;
      |%active List : 1;
      |%active Function : 2;
      |%active OperatingSystem : 2;
      |%active Status : 0;
      |%active Trap : 2;
      |%active Struct : 2;
      |%active String : 2;
      |%active File : 2;
      |%active VFS : 2;
      |%active Pointer : 0;
      |%active equal : 2;
      |%active ProcessStatus : 2;
      |%active ReadyList : 0;
      |%active DataSection : 0;
      |%active PogramSection : 0;
      |%active PCB : 0;
      |%active PID : 0;
      |%active Num1 : 1;
      |%active Num0 : 1;
      |%active ReadRequest : 1;
      |%active WriteOperation : 1;
      |%active LIdx : 2;
      |%active RIdx : 2;
      |%active QueueIdx : 2;
      |%active WriteRequest : 0;
      |
      |# Rules
      |%rule r_enter1 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[idle] | Num0[idle] | Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) -> ReadyList.(LIdx[idle,idle] | RIdx[idle,c:edge] | $2 | Process[idle,idle].($1 | $0 | PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[b:edge,idle])) | Num1[idle] | Num0[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==1};
      |
      |%rule r_enter2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[b:edge,c:edge] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[a:edge] | Num0[idle] | Plus[c:edge,b:edge,a:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.RIdx};
      |
      |%rule r_quit ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[b:edge,a:edge] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[c:edge] | Num0[idle] | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx==ReadyList.LIdx,ReadyList.Process.PCB.ProcessStatus==1,ReadyList.LIdx<ReadyList.RIdx,ReadyList.RIdx>ReadyList.Process.QueueIdx};
      |
      |%rule r_quit2 ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | Process[idle,idle].(PCB.(ProcessStatus[idle,idle] | PID) | QueueIdx[idle,idle] | $1 | $0) | $2) | Num1[idle] | Num0[idle] -> ReadyList.(LIdx[idle,idle] | RIdx[idle,idle] | $2) | Num1[a:edge] | Num0[idle] | Process[idle,idle].($0 | $1 | PCB.(PID | ProcessStatus[b:edge,c:edge])) | Plus[a:edge,b:edge,c:edge]{Condition:ReadyList.Process.QueueIdx<ReadyList.LIdx};
      |
      |%rule r_runRead Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | $0 | $1) -> read_write:Process[d:edge,idle].readf:Function[d:edge,idle]{};
      |
      |%rule r_runReadPrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | PogramSection.Function[idle,idle] | DataSection) | ReadRequest[idle] -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_runWrite Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1) -> read_write:Process[d:edge,idle].writef:Function[d:edge,idle]{};
      |
      |%rule r_runWritePrepare Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle]) | $0 | $1) | WriteRequest -> Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | WriteRequest) | $0 | $1){Condition:Process.PCB.ProcessStatus==2};
      |
      |%rule r_0 Process[idle,idle] | Num1[idle] -> Process[idle,idle].PCB.PID | Num1[idle]{};
      |
      |%rule r_1 Process[idle,idle].PCB.(ProcessStatus[idle,idle] | PID) | Num1[idle] -> Process[idle,idle].(PCB.(ProcessStatus[b:edge,c:edge] | PID) | PogramSection.Function[idle,idle] | DataSection) | Num1[a:edge] | Plus[c:edge,b:edge,a:edge]{Condition:Process.PCB.ProcessStatus==0};
      |
      |%rule r_2 Process[idle,idle].PCB.PID | Num1[idle] -> Process[idle,idle].PCB.(PID | ProcessStatus[idle,idle]) | Num1[idle]{};
      |
      |
      |
      |# prop
      |%prop p  P1:Process[idle,idle].(PCB.(PID | ProcessStatus[idle,idle] | ReadRequest[idle]) | PogramSection.Function[idle,idle] | DataSection){};
      |
      |
      |# Model
      |%agent  P1:Process[idle,idle] | RL:ReadyList.(LV:LIdx<0>[idle,idle] | RV:RIdx<0>[idle,idle]) | N1:Num1<1>[idle] | N0:Num0<0>[idle] | Read:ReadRequest[idle] | Write:WriteRequest | P2:Process[idle,idle];
      |
      |
      |
      |
      |
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |
      |""".stripMargin
}