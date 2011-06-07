package org.sqlman;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class LocalStatisticCollector extends StatisticCollector {

  /** . */
  private Object context;

  /** . */
  private int depth;

  /** . */
  private final Map<String, Map<Integer, Statistic>> state;

  /** . */
  private final ConcurrentStatisticCollector parent;

  LocalStatisticCollector(ConcurrentStatisticCollector parent, Configuration config) {
    super(config);

    //
    this.parent = parent;
    this.depth = 0;
    this.state = new HashMap<String, Map<Integer, Statistic>>();
  }

  private Map<Integer, Statistic> safeGetMap(String kind)
  {
    Map<Integer, Statistic> tmp = state.get(kind);
    if (tmp == null)
    {
      tmp = config.buildMap();
      state.put(kind, tmp);
    }
    return tmp;
  }

  Statistic getStatistic(String kind, int index, boolean create) {
    if (create) {
      Map<Integer, Statistic> tmp = state.get(kind);
      if (tmp != null) {
        return tmp.get(index);
      } else {
        return null;
      }
    } else {
      return safeGetMap(kind).get(index);
    }
  }

  Set<String> getKinds() {
    return state.keySet();
  }

  @Override
  void clear() {
    state.clear();
  }

  public int getDepth() {
    return depth;
  }

  void begin(Object context)
  {
    if (depth++ == 0) {
      this.context = context;
    }
  }

  void end()
  {
    if (--depth == 0)
    {
      if (state.size() > 0) {
        parent.merge(this);
        StringBuilder report = new StringBuilder("-= SQLMan request report =-\n");
        if (context != null) {
          report.append("request: ").append(context).append("\n");
        }
        report(report);
        System.out.println(report);
        clear();
      }
      context = null;
    }
  }
}
