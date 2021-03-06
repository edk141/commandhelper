package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 * @author layton
 */
public class InventoryManagement {
    public static String docs(){
        return "Provides methods for managing inventory related tasks.";
    }
    
    @api(environments={CommandHelperEnvironment.class})
    public static class pinv extends AbstractFunction {

        public String getName() {
            return "pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "mixed {[player, [index]]} Gets the inventory information for the specified player, or the current player if none specified. If the index is specified, only the slot "
                    + " given will be returned."
                    + " The index of the array in the array is 0 - 35, 100 - 103, which corresponds to the slot in the players inventory. To access armor"
                    + " slots, you may also specify the index. (100 - 103). The quick bar is 0 - 8. If index is null, the item in the player's hand is returned, regardless"
                    + " of what slot is selected. If there is no item at the slot specified, null is returned."
                    + " ---- If all slots are requested, an associative array of item objects is returned, and if"
                    + " only one item is requested, just that single item object is returned. An item object"
                    + " consists of the following associative array(type: The id of the item, data: The data value of the item,"
                    + " or the damage if a damagable item, qty: The number of items in their inventory, enchants: An array"
                    + " of enchant objects, with 0 or more associative arrays which look like:"
                    + " array(etype: The type of enchantment, elevel: The strength of the enchantment))";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.PlayerOfflineException, Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.RangeException};
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
            Integer index = -1;
            boolean all = false;
            MCPlayer m = null;
            if (args.length == 0) {
                all = true;
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else if (args.length == 1) {
                all = true;
                m = Static.GetPlayer(args[0], t);
            } else if (args.length == 2) {
                if (args[1] instanceof CNull) {
                    index = null;
                } else {
                    index = Static.getInt32(args[1], t);
                }
                all = false;
                m = Static.GetPlayer(args[0], t);
            }
			Static.AssertPlayerNonNull(m, t);
            if(all){
                CArray ret = CArray.GetAssociativeArray(t);
                for(int i = 0; i < 36; i++){
                    ret.set(i, getInvSlot(m, i, t), t);
                }
                for(int i = 100; i < 104; i++){
                    ret.set(i, getInvSlot(m, i, t), t);
                }
                return ret;
            } else {
                return getInvSlot(m, index, t);
            }
        }

        private Construct getInvSlot(MCPlayer m, Integer slot, Target t) {
            if(slot == null){
                return ObjectGenerator.GetGenerator().item(m.getItemInHand(), t);
            }
            MCPlayerInventory inv = m.getInventory();
            if(slot.equals(36)){
                slot = 100;
            }
            if(slot.equals(37)){
                slot = 101;
            }
            if(slot.equals(38)){
                slot = 102;
            }
            if(slot.equals(39)){
                slot = 103;
            }
            MCItemStack is;
            if(slot >= 0 && slot <= 35){
                is = inv.getItem(slot);
            } else if(slot.equals(100)){
                is = inv.getBoots();
            } else if(slot.equals(101)){
                is = inv.getLeggings();
            } else if(slot.equals(102)){
                is = inv.getChestplate();
            } else if(slot.equals(103)){
                is = inv.getHelmet();
            } else {
                throw new ConfigRuntimeException("Slot index must be 0-35, or 100-103", Exceptions.ExceptionType.RangeException, t);
            }
            return ObjectGenerator.GetGenerator().item(is, t);
        }
    }
	
    @api(environments = {CommandHelperEnvironment.class})
    public static class close_pinv extends AbstractFunction {

        public Exceptions.ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p;
			
			if (args.length == 1) {
				p = Static.GetPlayer(args[0], t);
			} else {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			
			p.closeInventory();
            
            return new CVoid(t);
        }

        public String getName() {
            return "close_pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[player]} Closes the inventory of the current player, "
					+ "or of the specified player.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api(environments={CommandHelperEnvironment.class})
    public static class set_pinv extends AbstractFunction {

        public String getName() {
            return "set_pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3, 4, 5, 7};
        }

        public String docs() {
            return "void {[player], pinvArray} Sets a player's inventory to the specified inventory object."
                    + " An inventory object is one that matches what is returned by pinv(), so set_pinv(pinv()),"
                    + " while pointless, would be a correct call. ---- The array must be associative, "
                    + " however, it may skip items, in which case, only the specified values will be changed. If"
                    + " a key is out of range, or otherwise improper, a warning is emitted, and it is skipped,"
                    + " but the function will not fail as a whole. A simple way to set one item in a player's"
                    + " inventory would be: set_pinv(array(2: array(type: 1, qty: 64))) This sets the player's second slot"
                    + " to be a stack of stone. set_pinv(array(103: array(type: 298))) gives them a hat. To set the"
                    + " item in hand, use something like set_pinv(array(null: array(type: 298))), where"
                    + " the key is null. If you set a null key in addition to an entire inventory set, only"
                    + " one item will be used (which one is undefined). Note that this uses the unsafe"
                    + " enchantment mechanism to add enchantments, so any enchantment value will work. If"
                    + " type uses the old format (for instance, \"35:11\"), then the second number is taken"
                    + " to be the data, making this backwards compatible (and sometimes more convenient).";

        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.PlayerOfflineException, Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            Construct arg;
            if(args.length == 2){
                m = Static.GetPlayer(args[0], t);
                arg = args[1];
            } else if(args.length == 1){
                arg = args[0];
            } else {
                throw new ConfigRuntimeException("The old format for set_pinv has been deprecated. Please update your script.", t);
            }
            if(!(arg instanceof CArray)){
                throw new ConfigRuntimeException("Expecting an array as argument " + (args.length==1?"1":"2"), Exceptions.ExceptionType.CastException, t);
            }
            CArray array = (CArray)arg;
			Static.AssertPlayerNonNull(m, t);
            for(String key : array.keySet()){
                try{
                    int index = -2;
                    try{
                        index = Integer.parseInt(key);
                    } catch(NumberFormatException e){
                        if(key.isEmpty()){
                            //It was a null key
                            index = -1;
                        } else {
                            throw e;
                        }
                    }
                    if(index == -1){
                        MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(""), t);
                        m.setItemInHand(is);
                    } else {
                        MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(index), t);
                        if(index >= 0 && index <= 35){
                            m.getInventory().setItem(index, is);
                        } else if(index == 100){
                            m.getInventory().setBoots(is);
                        } else if(index == 101){
                            m.getInventory().setLeggings(is);
                        } else if(index == 102){
                            m.getInventory().setChestplate(is);
                        } else if(index == 103){
                            m.getInventory().setHelmet(is);
                        } else {
                            ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found in array passed to set_pinv(), so ignoring.");
                        }
                    }
                } catch(NumberFormatException e){
                    ConfigRuntimeException.DoWarning("Expecting integer value for key in array passed to set_pinv(), but \"" + key + "\" was found. Ignoring.");
                }
            }
            return new CVoid(t);
        }
    }
    
    @api(environments={CommandHelperEnvironment.class})
	public static class phas_item extends AbstractFunction{

        public String getName() {
            return "phas_item";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "int {[player], itemId} Returns the quantity of the specified item"
                    + " that the player is carrying (including armor slots)."
                    + " This counts across all slots in"
                    + " inventory. Recall that 0 is false, and anything else is true,"
                    + " so this can be used to get the total, or just see if they have"
                    + " the item. itemId can be either a plain number, or a 0:0 number,"
                    + " indicating a data value.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.PlayerOfflineException, Exceptions.ExceptionType.FormatException,
                Exceptions.ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            String item;
            if(args.length == 1){
                item = args[0].val();
            } else {
                p = Static.GetPlayer(args[0], t);
                item = args[1].val();
            }
			Static.AssertPlayerNonNull(p, t);
            MCItemStack is = Static.ParseItemNotation(this.getName(), item, 0, t);
            MCPlayerInventory inv = p.getInventory();
            int total = 0;
            for(int i = 0; i < 36; i++){
                MCItemStack iis = inv.getItem(i);
                total += total(is, iis);
            }
            total += total(is, inv.getBoots());
            total += total(is, inv.getLeggings());
            total += total(is, inv.getChestplate());
            total += total(is, inv.getHelmet());
            return new CInt(total, t);
        }
        
        private int total(MCItemStack is, MCItemStack iis){
            if(iis.getTypeId() == is.getTypeId() && iis.getData().getData() == is.getData().getData()){
                int i = iis.getAmount();
                if(i < 0){
                    //Infinite stack
                    i = iis.maxStackSize();
                }
                return i;
            }         
            return 0;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api(environments={CommandHelperEnvironment.class})
	public static class pitem_slot extends AbstractFunction{

        public String getName() {
            return "pitem_slot";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "array {[player], itemID} Given an item id, returns the slot numbers"
                    + " that the matching item has at least one item in.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.FormatException,
                Exceptions.ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            String item;
            if(args.length == 1){
                item = args[0].val();
            } else {
                p = Static.GetPlayer(args[0], t);
                item = args[1].val();
            }
			Static.AssertPlayerNonNull(p, t);
            MCItemStack is = Static.ParseItemNotation(this.getName(), item, 0, t);
            MCPlayerInventory inv = p.getInventory();
            CArray ca = new CArray(t);
            for(int i = 0; i < 36; i++){
                if(match(is, inv.getItem(i))){
                    ca.push(new CInt(i, t));
                }
            }
            if(match(is, inv.getBoots())){
                ca.push(new CInt(100, t));
            }
            if(match(is, inv.getLeggings())){
                ca.push(new CInt(101, t));
            }
            if(match(is, inv.getChestplate())){
                ca.push(new CInt(102, t));
            }
            if(match(is, inv.getHelmet())){
                ca.push(new CInt(103, t));
            }
            return ca;
        }
        
        private boolean match(MCItemStack is, MCItemStack iis){
            return (is.getTypeId() == iis.getTypeId() && is.getData().getData() == iis.getData().getData());
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api(environments={CommandHelperEnvironment.class})
	public static class pgive_item extends AbstractFunction{

        public String getName() {
            return "pgive_item";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "int {[player], itemID, qty} Gives a player the specified item * qty."
                    + " Unlike set_pinv(), this does not specify a slot. The qty is distributed"
                    + " in the player's inventory, first filling up slots that have the same item"
                    + " type, up to the max stack size, then fills up empty slots, until either"
                    + " the entire inventory is filled, or the entire amount has been given."
                    + " The number of items actually given is returned, which will be less than"
                    + " or equal to the quantity provided. This function will not touch the player's"
                    + " armor slots however. Supports 'infinite' stacks by providing a negative number."
					+ " If the player's inv is full, 0 is returned in this case instead of the amount"
					+ " given.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.FormatException,
                Exceptions.ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            MCItemStack is;
			
            if(args.length == 2){
                is = Static.ParseItemNotation(this.getName(), args[0].val(), Static.getInt32(args[1], t), t);
            } else {
                p = Static.GetPlayer(args[0], t);
                is = Static.ParseItemNotation(this.getName(), args[1].val(), Static.getInt32(args[2], t), t);
            }

			Static.AssertPlayerNonNull(p, t);
            int total = is.getAmount();
            MCPlayerInventory inv = p.getInventory();
					
			if (total < 0) {
				for(int i = 0; i < 36; i++){
					MCItemStack iis = inv.getItem(i);
					
					if (iis.getTypeId() == 0) {
						inv.setItem(i, StaticLayer.GetItemStack(is.getTypeId(), is.getData().getData(), total));
						
						return new CInt(total, t);
					}
				}
				
				return new CInt(0, t);
			} else {
				int remaining = is.getAmount();
				
				for(int i = 0; i < 36; i++){
					MCItemStack iis = inv.getItem(i);
					
					if(remaining <= 0){
						break;
					}

					if(match(is, iis) || iis.getTypeId() == 0){
						//It's either the same item stack, or air.
						int currentQty = 0;
						int max = is.maxStackSize();

						if(iis.getTypeId() != 0){
							currentQty = iis.getAmount();
						}

						if(currentQty < 0){
							//Infinite stack. Assume max stack size.
							currentQty = is.maxStackSize();
						}

						int left = max - currentQty;
						int toGive;

						if(left < remaining){
							//We'll have to split this across more than this stack.
							toGive = left;
						} else {
							//We can distribute the rest in this stack
							toGive = remaining;
						}

						remaining -= toGive;

						//The total we are going to set the stack size to is toGive + currentQty
						int replace = toGive + currentQty;

						inv.setItem(i, StaticLayer.GetItemStack(is.getTypeId(), is.getData().getData(), replace));
					}
				}
				
				return new CInt(total - remaining, t);
			}
        }
       
        private boolean match(MCItemStack is, MCItemStack iis){
            return (is.getTypeId() == iis.getTypeId() && is.getData().getData() == iis.getData().getData());
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api(environments={CommandHelperEnvironment.class})
	public static class ptake_item extends AbstractFunction{

        public String getName() {
            return "ptake_item";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "int {[player], itemID, qty} Works in reverse of pgive_item(), but"
                    + " returns the number of items actually taken, which will be"
                    + " from 0 to qty.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.PlayerOfflineException,
                Exceptions.ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
            MCItemStack is;
            if(args.length == 2){
                is = Static.ParseItemNotation(this.getName(), args[0].val(), Static.getInt32(args[1], t), t);
            } else {
                p = Static.GetPlayer(args[0], t);
                is = Static.ParseItemNotation(this.getName(), args[1].val(), Static.getInt32(args[2], t), t);
            }
            int total = is.getAmount();
            int remaining = is.getAmount();
			Static.AssertPlayerNonNull(p, t);
            MCPlayerInventory inv = p.getInventory();
            for(int i = 35; i >= 0; i--){
                MCItemStack iis = inv.getItem(i);
                if(remaining <= 0){
                    break;
                }
                if(match(is, iis)){
                    //Take the minimum of either: remaining, or iis.getAmount()
                    int toTake = java.lang.Math.min(remaining, iis.getAmount());
                    remaining -= toTake;
                    int replace = iis.getAmount() - toTake;
                    if(replace == 0){
                        inv.setItem(i, StaticLayer.GetItemStack(0, 0));
                    } else {
                        inv.setItem(i, StaticLayer.GetItemStack(is.getTypeId(), is.getData().getData(), replace));
                    }
                }
            }
            return new CInt(total - remaining, t);
            
        }
        
        private boolean match(MCItemStack is, MCItemStack iis){
            return (is.getTypeId() == iis.getTypeId() && is.getData().getData() == iis.getData().getData());
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
	
	@api(environments={CommandHelperEnvironment.class})
	public static class get_inventory_item extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				w = p.getWorld();
			}
			
			MCInventory inv = GetInventory(args[0], w, t);			
			int slot = Static.getInt32(args[1], t);
			try{
				MCItemStack is = inv.getItem(slot);
				return ObjectGenerator.GetGenerator().item(is, t);
			} catch(ArrayIndexOutOfBoundsException e){
				throw new Exceptions.RangeException("Index out of bounds for the inventory type.", t);
			}
		}

		public String getName() {
			return "get_inventory_item";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "array {entityID, slotNumber | locationArray, slotNumber} If a number is provided, it is assumed to be an entity, and if the entity supports"
					+ " inventories, it will be valid. Otherwise, if a location array is provided, it is assumed to be a block (chest, brewer, etc)"
					+ " and interpreted thusly. Depending on the inventory type, the max index will vary. If the index is too large, a RangeException is thrown,"
					+ " otherwise, the item at that location is returned as an item array, or null, if no item is there. You can determine the inventory type"
					+ " (and thus the max index count) with get_inventory_type(). An itemArray, like the one used by pinv/set_pinv is returned.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class set_inventory_item extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				w = p.getWorld();
			}
			
			MCInventory inv = GetInventory(args[0], w, t);			
			int slot = Static.getInt32(args[1], t);
			MCItemStack is = ObjectGenerator.GetGenerator().item(args[2], t);
			try{
				inv.setItem(slot, is);
				return new CVoid(t);
			} catch(ArrayIndexOutOfBoundsException e){
				throw new Exceptions.RangeException("Index out of bounds for the inventory type.", t);
			}
		}

		public String getName() {
			return "set_inventory_item";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public String docs() {
			return "void {entityID, index, itemArray | locationArray, index, itemArray} Sets the specified item in the specified slot given either an entityID or a location array of a container"
					+ " object. See get_inventory_type for more information. The itemArray is an array in the same format as pinv/set_pinv takes.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class get_inventory_type extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				w = p.getWorld();
			}
			
			MCInventory inv = GetInventory(args[0], w, t);
			return new CString(inv.getType().name(), t);
		}

		public String getName() {
			return "get_inventory_type";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {entityID | locationArray} Returns the inventory type at the location specified, or of the entity specified. If the"
					+ " entity or location specified is not capable of having an inventory, a FormatException is thrown."
					+ " ---- Note that not all valid inventory types are actually returnable at this time, due to lack of support in the server, but"
					+ " the valid return types are: " + StringUtils.Join(MCInventoryType.values(), ", ");
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class get_inventory_size extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCWorld w = null;
			if(environment.getEnv(CommandHelperEnvironment.class).GetPlayer() != null){
				w = environment.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
			}
			MCInventory inventory = InventoryManagement.GetInventory(args[0], w, t);
			return new CInt(inventory.getSize(), t);
		}

		public String getName() {
			return "get_inventory_size";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "int {entityID | locationArray} Returns the max size of the inventory specified. If the block or entity can't have an inventory,"
					+ " a FormatException is thrown.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	private static MCInventory GetInventory(Construct specifier, MCWorld w, Target t){
		MCInventory inv;
		if(specifier instanceof CArray){
			MCLocation l = ObjectGenerator.GetGenerator().location(specifier, w, t);
			inv = StaticLayer.GetConvertor().GetLocationInventory(l);
		} else {
			int entityID = Static.getInt32(specifier, t);
			inv = StaticLayer.GetConvertor().GetEntityInventory(entityID);
		}
		if(inv == null){
			throw new Exceptions.FormatException("The entity or location specified is not capable of having an inventory.", t);
		} else {
			return inv;
		}
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class pinv_open extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer p1 = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			MCPlayer p2;
			if(args.length == 2){
				p1 = Static.GetPlayer(args[0], t);
				p2 = Static.GetPlayer(args[1], t);
			} else {
				p2 = Static.GetPlayer(args[0], t);
			}

			Static.AssertPlayerNonNull(p1, t);
			p1.openInventory(p2.getInventory());
			return new CVoid(t);
		}

		public String getName() {
			return "pinv_open";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {[playerToShow,] playerInventory} Opens a player's inventory, shown to the player specified's screen.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
    
//    @api
//    public static class pinv_consolidate extends AbstractFunction {
//        
//        public String getName() {
//            return "pinv_consolidate";
//        }
//        
//        public Integer[] numArgs() {
//            return new Integer[]{0, 1};
//        }
//        
//        public String docs() {
//            return "void {[player]} Consolidates a player's inventory as much as possible."
//                    + " There is no guarantee anything will happen after this function"
//                    + " is called, and there is no way to specify details about how"
//                    + " consolidation occurs, however, the following heuristics are followed:"
//                    + " The hotbar items will not be moved from the hotbar, unless there are"
//                    + " two+ slots that have the same item. Items in the main inventory area"
//                    + " will be moved closer to the bottom of the main inventory. No empty slots"
//                    + " will be filled in the hotbar.";
//        }
//        
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{};
//        }
//        
//        public boolean isRestricted() {
//            return true;
//        }
//        
//        public boolean preResolveVariables() {
//            return true;
//        }
//        
//        public Boolean runAsync() {
//            return false;
//        }
//        
//        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
//            MCPlayer p = environment.GetPlayer();
//            if(args.length == 1){
//                p = Static.GetPlayer(args[0], t);
//            }
//            //First, we need to address the hotbar
//            for(int i = 0; i < 10; i++){
//                //If the stack size is maxed out, we're done.
//            }
//            
//            return new CVoid(t);
//        }
//        
//        public CHVersion since() {
//            return CHVersion.V3_3_1;
//        }
//    }
}
