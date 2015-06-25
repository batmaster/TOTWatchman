<?php
  $sql = "SELECT * FROM timecheck LIMIT 10 ORDER BY id_th DESC";
  if (isset($_POST['sql'])) {
    $sql = $_POST["sql"];
  }
  $sql = str_replace("xxaxx", "'", $sql);
  $sql = str_replace("xxbxx", "(", $sql);
  $sql = str_replace("xxcxx", ")", $sql);
  $sql = str_replace("xxdxx", ">", $sql);
  $sql = str_replace("\\", "", $sql);

  header("content-type:text/javascript;charset=utf-8");
  $con = mysql_connect('203.114.104.242:3306','umbo','umbo')or die(mysql_error());
  mysql_select_db('guard')or die(mysql_error());
  mysql_query("SET NAMES UTF8");
  $res = mysql_query($sql);
  $logins="insert into logumbo values('','$res','$sqll')";
  $reslog = mysql_query($logins);
  //echo mysql_num_rows($res);
  if (mysql_num_rows($res) != 0) {
    while($row = mysql_fetch_array ($res)) {
		print_r($row);
    }
  }
  mysql_close();  
?> 
