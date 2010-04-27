/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;

public class Charinfo {
    public final Glob glob;
    public final String name;
    public int plgob;
    public Collection<Resource> paginae = new TreeSet<Resource>();
    public Map<String, CAttr> cattr = new HashMap<String, CAttr>();
    public Map<Integer, Buff> buffs = new TreeMap<Integer, Buff>();
    public Party party;
    
    public Charinfo(Glob glob, String name) {
	this.glob = glob;
	this.name = name;
	this.party = new Party(glob);
    }

    public static class CAttr extends Observable {
	String nm;
	int base, comp;
	
	public CAttr(String nm, int base, int comp) {
	    this.nm = nm.intern();
	    this.base = base;
	    this.comp = comp;
	}
	
	public void update(int base, int comp) {
	    if((base == this.base) && (comp == this.comp))
		return;
	    this.base = base;
	    this.comp = comp;
	    setChanged();
	    notifyObservers(null);
	}
    }
	
    public void paginaemsg(Object... args) {
	synchronized(paginae) {
	    int i = 0;
	    while(i < args.length) {
		String act = ((String)args[i++]).intern();
		String nm = (String)args[i++];
		int ver = (Integer)args[i++];
		Resource res = Resource.load(nm, ver);
		if(act == "+") {
		    paginae.add(res);
		} else if(act == "-") {
		    paginae.remove(res);
		}
	    }
	}
    }

    public void cattrmsg(Object... args) {
	synchronized(cattr) {
	    int i = 0;
	    while(i < args.length) {
		String nm = (String)args[i++];
		int base = (Integer)args[i++];
		int comp = (Integer)args[i++];
		CAttr a = cattr.get(nm);
		if(a == null) {
		    a = new CAttr(nm, base, comp);
		    cattr.put(nm, a);
		} else {
		    a.update(base, comp);
		}
	    }
	}
    }
    
    public void buffmsg(Object... args) {
	String name = ((String)args[0]).intern();
	synchronized(buffs) {
	    if(name == "clear") {
		buffs.clear();
	    } else if(name == "set") {
		int id = (Integer)args[1];
		Indir<Resource> res = glob.sess.getres((Integer)args[2]);
		String tt = (String)args[3];
		int ameter = (Integer)args[4];
		int nmeter = (Integer)args[5];
		int cmeter = (Integer)args[6];
		int cticks = (Integer)args[7];
		int fl = (Integer)args[8];
		boolean major = (fl & 1) != 0;
		Buff buff;
		if((buff = buffs.get(id)) == null) {
		    buff = new Buff(id, res);
		} else {
		    buff.res = res;
		}
		if(tt.equals(""))
		    buff.tt = null;
		else
		    buff.tt = tt;
		buff.ameter = ameter;
		buff.nmeter = nmeter;
		buff.ntext = null;
		buff.cmeter = cmeter;
		buff.cticks = cticks;
		buff.major = major;
		buff.gettime = System.currentTimeMillis();
		buffs.put(id, buff);
	    } else if(name == "rm") {
		int id = (Integer)args[1];
		buffs.remove(id);
	    }
	}
    }
}
