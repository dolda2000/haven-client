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
import java.awt.Color;

public class Party {
    Map<Integer, Member> memb = new TreeMap<Integer, Member>();
    Member leader = null;
    private Glob glob;
	
    public Party(Glob glob) {
	this.glob = glob;
    }
	
    public class Member {
	int gobid;
	private Coord c = null;
	Color col = Color.BLACK;
	
	public Coord getc() {
	    Gob gob;
	    if((gob = glob.oc.getgob(gobid)) != null)
		return(gob.getc());
	    return(c);
	}
    }
	
    public void msg(Object... args) {
	String mt = ((String)args[0]).intern();
	if(mt == "l") {
	    Map<Integer, Member> nmemb = new TreeMap<Integer, Member>();
	    for(int i = 1; i < args.length; i++) {
		int id = (Integer)args[i];
		Member m = memb.get(id);
		if(m == null) {
		    m = new Member();
		    m.gobid = id;
		}
		nmemb.put(id, m);
	    }
	    int lid = (leader == null)?-1:leader.gobid;
	    memb = nmemb;
	    leader = memb.get(lid);
	} else if(mt == "d") {
	    Member m = memb.get((Integer)args[1]);
	    if(m != null)
		leader = m;
	} else if(mt == "m") {
	    int i = 1;
	    Member m = memb.get((Integer)args[i++]);
	    Coord c = null;
	    int fl = (Integer)args[i++];
	    if((fl & 1) != 0)
		c = (Coord)args[i++];
	    Color col = m.col;
	    if((fl & 2) != 0)
		col = (Color)args[i++];
	    if(m != null) {
		m.c = c;
		m.col = col;
	    }
	}
    }
}
