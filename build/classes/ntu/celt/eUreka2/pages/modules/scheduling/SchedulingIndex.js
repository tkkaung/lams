function milestoneIconClick(milestoneId){
	$$(".task-row.icon_m_"+milestoneId).invoke("toggle");
	
	var phaseEmt = $$(".phase-row.icon_m_"+milestoneId);
	phaseEmt.each(function(item){
		var p_phaseId = item.identify();
		if($("icon_"+p_phaseId).hasClassName('phaseopen')){
			$$(".task2-row.icon_"+p_phaseId).invoke('hide');
			$("icon_"+p_phaseId).removeClassName('phaseopen');
			$("icon_"+p_phaseId).addClassName('phaseclose');
		}
		item.toggle();
	});
	$("icon_m_"+milestoneId).toggleClassName('milestoneopen');
	$("icon_m_"+milestoneId).toggleClassName('milestoneclose');

};
function phaseIconClick(p_phaseId){
	var taskEmt = $$(".task2-row.icon_"+p_phaseId);
	taskEmt.invoke('toggle');
	
	$("icon_"+p_phaseId).toggleClassName('phaseopen');
	$("icon_"+p_phaseId).toggleClassName('phaseclose');
}

document.observe("dom:loaded", function() {
	$$(".tooltip").invoke("hide");
	
	$$(".tooltipTrigger").each(function(item){
		item.observe("mouseover", function(event){
			var element = event.element();
			element.next(".tooltip").show();
		});	
	});
	$$(".tooltipTrigger").each(function(item){
		item.observe("mouseout", function(event){
			var element = event.element();
  			element.next(".tooltip").hide();
		});
	});
	
	 $$(".editPercentT").each(function(item){
		item.hide();
	});
	
	 $$('.todayBar').each(function(item){
		var h = item.up('td').getHeight();
		item.setStyle({
			height: h
		});
	});
});

function editPercDone(taskId){
	$('editPercentT'+taskId).show();
	$('percentT'+taskId).hide();
	$('editPercentT'+taskId).focus();
}
function editPercDoneBlur(taskId){
	$('editPercentT'+taskId).hide();
	$('percentT'+taskId).show();
}
function editPercDoneChange(taskId){
	$('ganttChartForm').submit();
}
	