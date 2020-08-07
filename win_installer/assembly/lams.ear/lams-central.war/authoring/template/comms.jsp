
		var validator;
		
		// configure the wizard. Call after doing var validator = $("#templateForm").validate({ 
		function initializeWizard(validatorIn) {
			validator = validatorIn;
	      	$('#rootwizard').bootstrapWizard({
	      		'nextSelector': '.button-next', 
	      		'previousSelector': '.button-previous',
	      		'onTabShow': function(tab, navigation, index) {
	      			var total = navigation.find('li').length;
	      			var current = index+1;
	      			var percent = (current/total) * 100;
	      			$('#rootwizard .progress-bar').css({width:percent+'%'})
 	      			if ( current == total) {
	      				$('.button-save').show();
	      				$('.button-next').hide();
	      			} else {
	      				$('.button-next').show();
	      				$('.button-save').hide();
	      			}
 	      		},
 	      		'tabClass': 'nav nav-pills',
 		  		'onNext': function(tab, navigation, index) {
 		  			if ( index > 1 ) {
	 		  			var valid = $("#templateForm").valid();
	 		  			if(!valid) {
	 		  				validator.focusInvalid();
	 		  				return false;
	 		  			}
	 		  		}
 		  		}
	      	});
	    }

		// Remove the display:none or the fields won't be validate as jquery validation is set to only valid non hidden fields. 
		// If we allow validation of hidden fields then we cannot have validation on the "next" button as it uses the hidden fields
		// to avoid validating the fields on other tabs. Still should not be seen as visibility is hidden. Also need to catch
		// the editor update and redo validation otherwise the "field blah is blank" error message won't go away when the user enters text.
		function reconfigureCKEditorInstance(instance) {
 			$('#'+instance.name).css('display','inline');
			instance.on('blur', function() { 
				$('#'+instance.name).valid();
			});
		}				

 		function refreshCKEditors() {
			// make sure all the ckeditors are refreshed, not just the validated ones
			for (var i in CKEDITOR.instances) {
           		CKEDITOR.instances[i].updateElement();
       		}
		}

		function validateCK(textarea) {
          	CKEDITOR.instances[textarea.id].updateElement(); // update textarea
          	var editorcontent = textarea.value.replace(/<[^>]*>/gi, ''); // strip tags
          	return editorcontent.length === 0;
		}
		
		function templateInvalidHandler(e, validator) {
			if (validator.numberOfInvalids()) {
				$("#templateErrorMessages").show();
			} else {
				$("#templateErrorMessages").hide();
			}
		}

		function submitForm(form) {
			$('#error-message').empty();
			refreshCKEditors();
       		
             var jqxhr = $.ajax( {
	        		method: $(form).attr('method'),
					url: getSubmissionURL(),
                	data: $(form).serialize()
	            	})
				.done(function(data) {
        			var learningDesignID = data.learningDesignID;
        			if ( ! learningDesignID) {
        				var errors = data.errors;
        				if ( errors ) {
        					$('#templateErrorMessages').show();
        					$('#error-message').show();
	        				for (var i = 0; i < errors.length; i++) {
	        					$('#error-message').append('<li class="text-danger">'+errors[i]+'</li>');
	        				}
        				} else {
        					var fatal = data.fatal;
        					if ( fatal ) {
        						alert(fatal);
        					} else { 
	        					alert('Save failed (expected parameters missing). Data returned by server was '+data);
	        				}
	        			}
				    } else {
					    var title = encodeURIComponent(data.title);
					    location.href='<lams:WebAppURL />authoring/template/createresult.jsp?learningDesignID='+learningDesignID
					    		+'&learningDesigntitle='+title;
       				}})
				.fail(function() {
					alert('Save failed. Please see the server logs for more details.\n\n');
					});
		}
		
		function getSubmissionURL() {
			return '<lams:WebAppURL />/authoring/template/'+$('#template').val().toLowerCase()+'.do';
		}
		
		function doGotoList() {
		 	if(confirm("<fmt:message key='authoring.msg.list.cancel.save'/>")){
		 		location.href = '<lams:WebAppURL />authoring/template/list.jsp';
		 	}
		 }

		function doCancel() {
		 	if(confirm("<fmt:message key='authoring.msg.close.cancel.save'/>")){
		 		closeWindow();
		 	}
		}
		// Called by save button
		function doSaveForm() {
			$('#templateForm').submit();
		}


		function createAssessment(questionType) {
			var type = questionType ? questionType : 'essay';
			var currNum = $('#numAssessments').val();
			var nextNum = +currNum + 1;
			var newDiv = document.createElement("div");
			newDiv.id = 'divassess'+nextNum;
			newDiv.className = 'space-top';
			var url=getSubmissionURL()+"?method=createAssessment&questionNumber="+nextNum+"&questionType="+type;
			$('#divassessments').append(newDiv);
			$.ajaxSetup({ cache: true });
			$(newDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					newDiv.remove();
				} else {
					$('#numAssessments').val(nextNum);
					newDiv.scrollIntoView();
				}
			});
		}		
		
		function createQuestion(numQuestionsFieldname, newDivPrefix, questionDivName, forward, extraParam) {
			var numQuestions = $('#'+numQuestionsFieldname);
			var currNum = numQuestions.val();
			var nextNum = +currNum + 1;
			var newDiv = document.createElement("div");
			newDiv.id = newDivPrefix+nextNum;
			var url=getSubmissionURL()+"?method=createQuestion&questionNumber="+nextNum;
			if ( forward && forward.length > 0) {
				url = url + "&forward=" + forward;
			}
			if ( extraParam ) {
				url = url + extraParam;
			}
			$('#'+questionDivName).append(newDiv);
			$.ajaxSetup({ cache: true });
			$(newDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					newDiv.remove();
				} else {
					numQuestions.val(nextNum);
					newDiv.focus();
					newDiv.scrollIntoView();
				}
			});
		}		

		function createOption(questionNum, maxOptionCount) {
			var currNum = $('#numOptionsQuestion'+questionNum).val();
			var nextNum = +currNum + 1;
			var newDiv = document.createElement("div");
			newDiv.id = 'divq'+questionNum+'opt'+nextNum;
			var optionsDiv=$('#divq'+questionNum+'options');
			var lastChild=optionsDiv.children().filter(':last');
			$(lastChild).after(newDiv);

			var url=getSubmissionURL()+"?method=createOption&questionNumber="+questionNum+"&optionNumber="+nextNum;
			$.ajaxSetup({ cache: true });
			$(newDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					newDiv.remove();
				} else {
					$('#numOptionsQuestion'+questionNum).val(nextNum);
					if ( nextNum >= maxOptionCount  ) {
						$('#createOptionButton'+questionNum).hide();
					}
					// need to add the down button to the previous last option!
					var image = document.getElementById('question'+questionNum+'option'+currNum+'DownButton')
					image.style.display="inline";
					newDiv.scrollIntoView();
					
				}
			});
		}		
				
		function createAssessmentOption(questionNum, maxOptionCount) {
			var currNum = $('#assmcq'+questionNum+'numOptions').val();
			var nextNum = +currNum + 1;
			var newDiv = document.createElement("div");
			newDiv.id = 'divassmcq'+questionNum+'opt'+nextNum;
			var optionsDiv=$('#divassmcq'+questionNum+'options');
			var lastChild=optionsDiv.children().filter(':last');
			$(lastChild).after(newDiv);

			var url=getSubmissionURL()+"?method=createOption&questionNumber="+questionNum+"&optionNumber="+nextNum+"&assess=true";
			$.ajaxSetup({ cache: true });
			$(newDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					newDiv.remove();
				} else {
					$('#assmcq'+questionNum+'numOptions').val(nextNum);
					if ( nextNum >= maxOptionCount  ) {
						$('#createAssessmentOptionButton'+questionNum).hide();
					}
					// need to add the down button to the previous last option!
					var image = document.getElementById('assmcq'+questionNum+'option'+currNum+'DownButton')
					image.style.display="inline";
					newDiv.scrollIntoView();
					
				}
			});
		}		

		function getOptionData(questionNum, assessment) {
			var paramPrefix  =  assessment ? "assmcq" : "question"; 
			paramPrefix = paramPrefix + questionNum;
			var data = { };
			var correctField = paramPrefix + "correct";
			$('#templateForm').find('input, textarea, select').each(function() {
				if ( this.name == correctField ) {
					if ( this.checked ) {
			    			data[this.name] = $(this).val();
					}
				} else if ( this.name.startsWith(paramPrefix) ) {
		    			data[this.name] = $(this).val();
		    		}
		    });
		    return data;
		}
		
		function swapOptions(questionNum, optionNum1, optionNum2, divToLoad, assessment) {
			refreshCKEditors() ;
			var url=getSubmissionURL()+"?method=swapOption&questionNumber="+questionNum+"&optionNumber1="+optionNum1+"&optionNumber2="+optionNum2;
			if ( assessment ) {
				url += "&assess=true";
			}
			var data = getOptionData(questionNum, assessment);

			$.ajaxSetup({ cache: true });
			jqueryDivToLoad = divToLoad ? $('#'+divToLoad) : $('#divq'+questionNum+'options');
			jqueryDivToLoad.load(url, data, function( response, status, xhr ) {
				if ( status == "error" ) {
					alert("Swap failed. See server logs for details. "+xhr.statusText);
					console.log( xhr.status + " " + xhr.statusText );
				} 
			});
		}

		function removeOption(questionNum, optionNum, divToLoad, assessment) {
			refreshCKEditors() ;
			var url=getSubmissionURL()+"?method=deleteOption&questionNumber="+questionNum+"&optionNumber="+optionNum;
			if ( assessment ) 
				url += "&assess=true";
			var data = getOptionData(questionNum, assessment);
				
			$.ajaxSetup({ cache: true });
			jqueryDivToLoad = divToLoad ? $('#'+divToLoad) : $('#divq'+questionNum+'options');
			jqueryDivToLoad.load(url, data, function( response, status, xhr ) {
				if ( status == "error" ) {
					alert("Delete failed. See server logs for details. "+xhr.statusText);
					console.log( xhr.status + " " + xhr.statusText );
				} else {
						$('#createOptionButton'+questionNum).show();
				}
			});
		}

		function createForum(numTopicsFieldname, newDivPrefix, forumDivName, forward) {
			var numTopics = $('#'+numTopicsFieldname);
			var currNum = numTopics.val();
			var nextNum = +currNum + 1;
			var newDiv = document.createElement("div");
			newDiv.id = newDivPrefix+nextNum;
			var url=getSubmissionURL()+"?method=createForum&topicNumber="+nextNum;
			if ( forward && forward.length > 0) {
				url = url + "&forward=" + forward;
			}
			$('#'+forumDivName).append(newDiv);
			$.ajaxSetup({ cache: true });
			$(newDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					newDiv.remove();
				} else {
					numTopics.val(nextNum);
					newDiv.focus();
					newDiv.scrollIntoView();
				}
			});
		}		

		function createURL(numURLsFieldname, newDivPrefix, urlDivName, extraParam) {
			var numUrls = $('#'+numURLsFieldname);
			var currNumURLS = numUrls.val();
			var nextNumURLS = +currNumURLS + 1;
			var urlDiv = document.createElement("div");
			urlDiv.id = newDivPrefix+nextNumURLS;
			var url=getSubmissionURL()+"?method=createResource&urlNumber="+nextNumURLS;
			if ( extraParam ) {
				url = url + extraParam;
			}
			$('#'+urlDivName).append(urlDiv);
			$.ajaxSetup({ cache: true });
			$(urlDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					urlDiv.remove();
				} else {
					numUrls.val(nextNumURLS);
				}
			});
		}		
		function createBranch() {
			var currNum = $('#numberOfBranches').val();
			var nextNum = +currNum + 1;
			var branchDiv = document.createElement("div");
			branchDiv.id = 'divbranch'+nextNum;
			var url=getSubmissionURL()+"?method=createBranch&branchNumber="+nextNum;
			$('#divbranches').append(branchDiv);
			$.ajaxSetup({ cache: true });
			$(branchDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					$('#divurls').remove();
				} else {
					$('#numberOfBranches').val(nextNum);
				}
			});
		}		
		
		function createPeerReviewCriteria() {
			var currNum = $('#numRatingCriterias').val();
			var nextNum = +currNum + 1;
			var newDiv = document.createElement("div");
			newDiv.id = 'divrating'+nextNum;
			newDiv.className = 'space-top';
			var url=getSubmissionURL()+"?method=createRatingCriteria&criteriaNumber="+nextNum;
			$('#divratings').append(newDiv);
			$.ajaxSetup({ cache: true });
			$(newDiv).load(url, function( response, status, xhr ) {
				if ( status == "error" ) {
					console.log( xhr.status + " " + xhr.statusText );
					newDiv.remove();
				} else {
					$('#numRatingCriterias').val(nextNum);
					newDiv.scrollIntoView();
				}
			});
		}		
		
		function testURL(urlField) {
			launchPopup($('#'+urlField).val(),'popupUrl');
		}
