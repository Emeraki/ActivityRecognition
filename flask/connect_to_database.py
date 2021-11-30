import pymysql
import time


def saveData(mid_time, activity, watchId):
    m_time = round(mid_time / 1000)
    b_time = m_time - 3
    e_time = m_time + 2

    begin_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(b_time))
    true_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(m_time))
    end_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(e_time))

    # 打开数据库连接
    db = pymysql.connect(host='localhost',
                         user='root',
                         password='111',
                         database='smart_home_lab')

    # 使用 cursor() 方法创建一个游标对象 cursor
    cursor = db.cursor()

    # 先对数据检查，上下2s内有重复动作，则不会存储
    sql_check = 'select `activity`,`activityTime` from `test_recognize_activity` where `activityTime` between \'%s\' and \'%s\' and `activity` = \'%s\' and ' \
                '`watchId` = \'%s\'' % (begin_time, end_time, activity, watchId)

    cursor.execute(sql_check)

    # 使用 fetchone() 方法获取单条数据.
    result_check = cursor.fetchall()

    if (len(result_check)) > 0:
        print("近3秒内有重复动作，此条不保存")
    else:
        sql_insert = 'insert into `test_recognize_activity`(`watchId`,`activity`,`activityTime`) values (\'%s\',\'%s\',\'%s\')' % (
            watchId, activity, true_time)

        try:
            cursor.execute(sql_insert)
            db.commit()
        except:
            db.rollback()

        print("插入数据")

    # 关闭数据库连接
    db.close()
