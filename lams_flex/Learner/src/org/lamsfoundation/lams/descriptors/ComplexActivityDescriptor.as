package org.lamsfoundation.lams.descriptors
{
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ICollectionView;
	import mx.controls.treeClasses.ITreeDataDescriptor;
	
	import org.lamsfoundation.lams.events.WizardEvent;

	public class ComplexActivityDescriptor implements ITreeDataDescriptor
	{

		public function ComplexActivityDescriptor() {
		}
		
		public function getChildren(node:Object, model:Object=null):ICollectionView
		{
			return node.children;
		}
		
		public function hasChildren(node:Object, model:Object=null):Boolean
		{
			return (node.children.length > 0);
		}
		
		public function isBranch(node:Object, model:Object=null):Boolean
		{
			if(node is WorkspaceItem && node.resourceType == WorkspaceItem.RT_FOLDER) {
				return true;
			}
				
			return false;
		}
		
		public function getData(node:Object, model:Object=null):Object
		{
			if(node is WorkspaceItem && node.resourceType == WorkspaceItem.RT_FOLDER) {
				return {resourceID: node.resourceID, children:node.children};
			}
			return null;
		}
		
		public function addChildAt(parent:Object, newChild:Object, index:int, model:Object=null):Boolean
		{
			return false;
		}
		
		public function removeChildAt(parent:Object, child:Object, index:int, model:Object=null):Boolean
		{
			return false;
		}

	}
}