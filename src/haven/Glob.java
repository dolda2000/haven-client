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

public class Glob {
    public static final int GMSG_TIME = 0;
    public static final int GMSG_ASTRO = 1;
    public static final int GMSG_LIGHT = 2;
	
    public long time;
    public Astronomy ast;
    public OCache oc = new OCache(this);
    public MCache map;
    public Session sess;
    public java.awt.Color amblight = null;
    
    public Glob(Session sess) {
	this.sess = sess;
	map = new MCache(sess);
    }
    
    private static double defix(int i) {
	return(((double)i) / 1e9);
    }
	
    public void blob(Message msg) {
	while(!msg.eom()) {
	    switch(msg.uint8()) {
	    case GMSG_TIME:
		time = msg.int32();
		break;
	    case GMSG_ASTRO:
		double dt = defix(msg.int32());
		double mp = defix(msg.int32());
		double yt = defix(msg.int32());
		boolean night = (dt < 0.25) || (dt > 0.75);
		ast = new Astronomy(dt, mp, yt, night);
		break;
	    case GMSG_LIGHT:
		amblight = msg.color();
		break;
	    }
	}
    }
}
