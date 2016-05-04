package com.ltbrew.brewbeer.api.longconnection.process;

import android.content.Context;
import android.content.SharedPreferences;

import com.ltbrew.brewbeer.api.model.Direct_push;

/**
 * Created by Jason on 2015/6/23.
 */
public class ManageLongConnIp {

    private SharedPreferences longConnSp;
    private static final String LONGCONN_SP_TITLE = "LongConnHost";
    private static final String PORT_INDEX_KEY = "portIndex";
    public Context context;
    public String ipHost;
    public int port;
    Direct_push direct_push;
    static ManageLongConnIp manageLongConnIp;
    int portIndex = 0;

    private ManageLongConnIp() {
    }

    public static ManageLongConnIp getInstance() {
        if (manageLongConnIp == null)
            manageLongConnIp = new ManageLongConnIp();
        return manageLongConnIp;

    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void initDirect_push(Context context, Direct_push direct_push) {
        this.direct_push = direct_push;
        longConnSp = context.getApplicationContext().getSharedPreferences(LONGCONN_SP_TITLE,
                Context.MODE_PRIVATE);
        setPort();
    }

    public void setPort() {
        if (!getLongConnIPFromLocal() && direct_push != null) {
            port = this.direct_push.getPorts().get(portIndex);
        }
    }

    public boolean getLongConnIPFromLocal() {
        if (direct_push == null)
            return false;
        ipHost = this.direct_push.getHost();
        port = longConnSp.getInt(PORT_INDEX_KEY, 0);
        if (port == 0)
            return false;
        return true;
    }

    public void switchPort() {
        if (direct_push == null)
            return;
        if (direct_push.getPorts().size() > portIndex) {
            portIndex++;
            port = direct_push.getPorts().get(portIndex);

        } else {
            portIndex = 0;
            port = direct_push.getPorts().get(portIndex);
        }
    }

}
