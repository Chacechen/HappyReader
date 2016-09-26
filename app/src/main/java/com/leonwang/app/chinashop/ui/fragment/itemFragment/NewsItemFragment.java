package com.leonwang.app.chinashop.ui.fragment.itemFragment;

import android.os.Bundle;
import android.view.View;

import com.leonwang.app.chinashop.R;
import com.leonwang.app.chinashop.adapter.RefreshCallback;
import com.leonwang.app.chinashop.adapter.TopNewsAdapter;
import com.leonwang.app.chinashop.base.RxLazyBaseFragment;
import com.leonwang.app.chinashop.entity.TopNewsEntity;
import com.leonwang.app.chinashop.iml.SwipeItemCallback;
import com.leonwang.app.chinashop.net.RetrofitHelper;
import com.leonwang.app.chinashop.utils.ConstantUtils;
import com.leonwang.app.chinashop.utils.LogUtils;
import com.leonwang.app.chinashop.widget.SwipeRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 当前类注释：新闻页面子条目
 * Author :LeonWang
 * Created  2016/9/22.15:24
 * Description:
 * E-mail:lijiawangjun@gmail.com
 */

public class NewsItemFragment extends RxLazyBaseFragment {


    @BindView(R.id.swipeRecyclerView)
    SwipeRecyclerView mSwipeRecyclerView;

    private ConstantUtils.ENewsType mENewsType;
    private TopNewsAdapter mTopNewsAdapter;
    private ArrayList<TopNewsEntity.ResultEntity.DataEntity> mArrayList = new ArrayList<>();

    //当前页数
    int curpageInt = 1;
    //总页数 -----模拟测试 具体根据后台返回做判断
    int totalPagerInt = 5;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_news_item;
    }

    @Override
    protected void finishCreateView(Bundle savedInstanceState) {
        isPrepared = true;
//        initData();
        LogUtils.d("我被初始化了");
        lazyLoad();
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) return;
        initData();
        isPrepared = false;
    }

    private void initData() {

        mSwipeRecyclerView.setRefreshCallback(new RefreshCallback() {
            //下拉刷新
            @Override
            public void downRefresh() {
//                Toast.makeText(getApplicationContext(),"下拉刷新",Toast.LENGTH_SHORT).show();
                if (mArrayList != null) mArrayList.clear();
                curpageInt = 1;
                getNetData(true);
            }

            //加载更多
            @Override
            public void upRefresh(int count) {
//                getNetData();
                if (curpageInt < totalPagerInt) {
                    curpageInt = ++curpageInt;
                    getNetData(true);
                } else {
                    getNetData(false);
                }

            }
        });

        mTopNewsAdapter = new TopNewsAdapter(getApplicationContext(), new SwipeItemCallback<TopNewsEntity.ResultEntity.DataEntity>() {
            @Override
            public void callback(View view, int position, TopNewsEntity.ResultEntity.DataEntity dataEntity) {

            }
        });

        mSwipeRecyclerView.setAdapter(mTopNewsAdapter);
        mSwipeRecyclerView.startDownRefresh();

    }

    public void setNewsType(ConstantUtils.ENewsType type) {
        mENewsType = type;
    }



    private void getNetData(final boolean isLoadMore) {
        RetrofitHelper.getTopNewsApi()
                .getNews(mENewsType.getType(),ConstantUtils.NEWS_APIKEY)
                .compose(this.<TopNewsEntity>bindToLifecycle())
                .flatMap(new Func1<TopNewsEntity, Observable<?>>() {
                    @Override
                    public Observable<?> call(TopNewsEntity topNewsEntity) {
                        mArrayList = topNewsEntity.getResult().getData();
                        return Observable.just("onNext");
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object s) {
                        mTopNewsAdapter.load(mArrayList,isLoadMore);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                      mSwipeRecyclerView.downRefreshComplete(3);
                    }
                });
    }


}
