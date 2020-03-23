package me.Straiker123;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;


public class ScoreboardAPIV2 {
    private Scoreboard v;
    private String title;
    private Player ss;
    private Objective dd;
    private HashMap<Integer,String> a=new HashMap<Integer, String>(),entry=new HashMap<Integer,String>();
    private boolean send;
    private int cu;
    @SuppressWarnings("deprecation")
	public ScoreboardAPIV2(Player p) {
    	this.ss=p;
    	title = "TheAPI";
    	send=false;
    	v=p.getServer().getScoreboardManager().getNewScoreboard();
    	if(v.getObjective("a")==null) {
    		dd=v.registerNewObjective("a", "dummy");
    	}else
    		dd=v.getObjective("a");
		dd.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    
    public void setTitle(String title) {
    	if(title!=null && !title.equals(""))
    	this.title=TheAPI.colorize(title);
    }
    public void addLine(String line) {
    	a.put(15-cu,TheAPI.colorize(line));
    	++cu;
    }
    
    public Scoreboard getScoreboard() {
    	return v;
    }
    
    public Objective getObjective() {
    	return dd;
    }
    
    public Player getPlayer() {
    	return ss;
    }
    
    public void setLines(List<String> lines) {
    	for(String d : lines)addLine(d);
    }

	public void create() {
		cu=0;
		Scoreboard b = ss.getScoreboard();
		if(!send || send && b != v && b.getObjectives().isEmpty()||b==null) {
			send=true;
	    	ss.setScoreboard(v);
		}
		if(!dd.getDisplayName().equals(title))
		dd.setDisplayName(title);
		for(Integer i : a.keySet()) {
			String ne = a.get(i);
			if(entry.containsKey(i)) {
				String old = entry.get(i);
			if(!ne.equals(old)) {
				v.resetScores(old);
				dd.getScore(ne).setScore(i);
			}}else
				if(!entry.containsKey(i))
					dd.getScore(ne).setScore(i);
		}
		entry=a;
		a=new HashMap<Integer,String>();
	}
}
