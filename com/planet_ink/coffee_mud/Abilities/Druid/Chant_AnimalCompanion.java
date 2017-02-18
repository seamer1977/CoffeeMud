package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2017 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Chant_AnimalCompanion extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_AnimalCompanion";
	}

	private final static String	localizedName	= CMLib.lang().L("Animal Companion");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Animal Companion)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT-90;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(!CMLib.flags().isAnimalIntelligence(target))
		{
			mob.tell(L("This chant only works on animals."));
			return false;
		}
		if(target.amFollowing()!=mob)
		{
			mob.tell(L("This chant only works on animal followers."));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		
		if(target.isInCombat())
		{
			mob.tell(target,null,null,L("Not while <S-NAME> <S-IS-ARE> fighting!"));
			return false;
		}
		
		if(CMLib.flags().isSleeping(target) || (!CMLib.flags().canBeHeardSpeakingBy(mob,target)))
		{
			mob.tell(target,null,null,L("<S-NAME> cannot make the oath with you right now!"));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) an oath to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					Ability A=a.nextElement();
					if((A!=null)&&((A.flags()&Ability.FLAG_CHARMING)!=0)&&(A.canBeUninvoked()))
					{
						A.unInvoke();
						mob.makePeace(true);
						target.makePeace(true);
						if(target.amFollowing()!=mob)
							target.setFollowing(mob);
					}
				}
				mob.makePeace(true);
				target.makePeace(true);
				if(target.amFollowing()!=mob)
					target.setFollowing(mob);
				Ability A=target.fetchEffect("Loyalty");
				if(A==null)
				{
					A=CMClass.getAbility("Loyalty");
					A.setMiscText("NAME="+mob.Name());
					A.setSavable(true);
					target.addNonUninvokableEffect(A);
					mob.tell(mob,target,null,L("<T-NAME> is now loyal to you."));
					mob.location().recoverRoomStats();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) an oath to <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
