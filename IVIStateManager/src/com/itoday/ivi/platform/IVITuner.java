package com.itoday.ivi.platform;

/**
 * 收音机常量定义
 * @author Yijun
 *
 */
public class IVITuner {
	
	/**
	 * 当前播放频率
	 */
	//public static final String FREQ = "freq";
	
	/**
	 * 当前波段
	 */
	public static final String BAND = "band";
	
	/**
	 * 当前电台名称
	 */
	public static final String STATION = "station";
	
	/**
	 * 收音机状态
	 */
	public static final String STATE = "tuner_state";
	
	/**
	 * 当前收银区域
	 */
	public static final String AREA = "area";
	
	/**
	 * 立体声状态
	 */
	public static final String STEREO = "stereo";
	
	/**
	 * 本地或远程搜索状态
	 */
	public static final String LOC_FAR = "loc.far";
	
	/**
	 * 当前播放的FM频率
	 */
	public static final String LAST_FM_FREQ = "last_fm_freq";
	
	/**
	 * 当前播放的AM频率
	 */
	public static final String LAST_AM_FREQ = "last_am_freq";
	
	/**
	 * 当前播放的FM索引
	 */
	public static final String LAST_FM_INDEX = "last_fm_index";
	
	/**
	 * 当前播放的AM索引
	 */
	public static final String LAST_AM_INDEX = "last_am_index";
	
	
	/**
	 * 收音模块的运行状态
	 * @author iToday
	 *
	 */
	public class Status{
	
		/**
		 * 正在播放
		 */
		public static final int PLAYING = 0;
		
		/**
		 * 正在扫描
		 */
		public static final int SCANNING = 1;
		
		/**
		 * 收音机关闭
		 */
		public static final int CLOSED = 2;
	}
	
	/**|
	 * 扫描类型
	 * @author iToday
	 *
	 */
	public class Scan{
		/**
		 * 扫描停止
		 */
		public static final int SCAN_NONE = 0;
		
		/**
		 * 上扫描
		 */
		public static final int SCAN_UP = 1;
		
		/**
		 * 下扫描
		 */
		public static final int SCAN_DOWN = 2;
		
		/**
		 * 扫描保存
		 */
		public static final int SCAN_SAVE = 3;
	}

	/**
	 * 收音区域定义
	 * @author iToday
	 *
	 */
	public class Area{
		/**
		 * 泰国
		 */
		public static final int AREA_THAILAND = 0;
		
		/**
		 * 欧洲
		 */
		public static final int AREA_EUROPE = 1;
		
		/**
		 * 拉丁美洲
		 */
		public static final int AREA_LATIN = 2;
		
		/**
		 * 北美洲
		 */
		public static final int AREA_NORTH = 3;
		
		/**
		 * 日本
		 */
		public static final int AREA_JAPAN = 4;
		
		/**
		 * 俄罗斯
		 */
		public static final int AREA_RUSSIA = 5;
		
		/**
		 * 区域个数
		 */
		public static final int AREA_NUM = 6;
	}
	
	/**
	 * 波段定义
	 * @author iToday
	 *
	 */
	public class Band{
		/**|
		 * 波段FM
		 */
		public static final int FM = 1;
		
		/**
		 * 波段AM
		 */
		public static final int AM = 0;
	}
	
	/**
	 * 服务控制远程界面的命令定义
	 * @author iToday
	 *
	 */
	public class RemoteCommand{
		
		/**
		 * 退出
		 */
		public static final int EXIT = 1;
		
		/**
		 * 隐藏
		 */
		public static final int HIDE = 2;
		
		/**
		 * 显示
		 */
		public static final int SHOW = 3;
	}
	
	/**
	 * 立体声 开关定义
	 * @author iToday
	 */
	public class Stereo{
		
		/**
		 * 打开
		 */
		public static final int OPEN = 1;
		
		/**
		 * 关闭
		 */
		public static final int CLOSE = 0;
		
		/**
		 * 立体声状态
		 */
		public static final int STEREO = 1;
		
		/**
		 * 单声道
		 */
		public static final int MONO = 0;
	}
	
	/**
	 * FM模式
	 * @author Yijun
	 *
	 */
	public class FmMode{
		
		/**
		 * 本地
		 */
		public static final int LOC = 0;
		
		
		/**
		 * 远程
		 */
		public static final int FAR = 1;
	}
}


