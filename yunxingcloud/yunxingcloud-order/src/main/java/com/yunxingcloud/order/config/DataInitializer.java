package com.yunxingcloud.order.config;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductCategoryRepository catRepo;
    private final ProductBrandRepository brandRepo;
    private final ProductRepository productRepo;
    private final ProductSkuRepository skuRepo;
    private final BannerRepository bannerRepo;
    private final CouponRepository couponRepo;
    private final ProductReviewRepository reviewRepo;

    public DataInitializer(ProductCategoryRepository catRepo, ProductBrandRepository brandRepo,
                           ProductRepository productRepo, ProductSkuRepository skuRepo,
                           BannerRepository bannerRepo, CouponRepository couponRepo,
                           ProductReviewRepository reviewRepo) {
        this.catRepo = catRepo; this.brandRepo = brandRepo;
        this.productRepo = productRepo; this.skuRepo = skuRepo;
        this.bannerRepo = bannerRepo; this.couponRepo = couponRepo;
        this.reviewRepo = reviewRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (catRepo.count() > 0) return; // already initialized
        initCategories();
        initBrands();
        initProducts();
        initBanners();
        initCoupons();
        initReviews();
    }

    private void initCategories() {
        String[][] cats = {
            {"手机通讯", "📱"}, {"电脑办公", "💻"}, {"家电家具", "🏠"},
            {"服饰鞋包", "👗"}, {"美妆护肤", "💄"}, {"食品生鲜", "🍎"},
            {"母婴玩具", "🍼"}, {"运动户外", "⚽"}, {"图书文娱", "📚"},
            {"汽车用品", "🚗"}, {"医药健康", "💊"}, {"珠宝配饰", "💎"},
        };
        int sort = 0;
        for (String[] c : cats) {
            ProductCategory cat = new ProductCategory();
            cat.setName(c[0]); cat.setIcon(c[1]); cat.setSortOrder(sort++);
            catRepo.save(cat);
        }
    }

    private void initBrands() {
        String[] brands = {"华为", "苹果", "小米", "三星", "索尼", "戴尔", "联想", "耐克",
                "阿迪达斯", "欧莱雅", "茅台", "海尔", "格力", "美的", "良品铺子"};
        for (String b : brands) {
            ProductBrand brand = new ProductBrand(); brand.setName(b); brandRepo.save(brand);
        }
    }

    private void initProducts() {
        List<ProductCategory> cats = catRepo.findAll();
        List<ProductBrand> brands = brandRepo.findAll();

        Object[][] data = {
            // {name, desc, priceYuan, stock, sales, categoryIdx, brandIdx, isNew, isHot, tags}
            {"华为 Mate 80 Pro 5G 旗舰手机 12GB+512GB", "麒麟9100芯片 | 2K曲面屏 | 100W超级快充 | 卫星通信", 699900L, 500, 3280, 0, 0, false, true, "[\"5G\",\"旗舰\",\"拍照\"]"},
            {"iPhone 18 Pro Max 256GB 暗紫色", "A20仿生芯片 | 灵动岛 | 4800万像素 | 钛金属边框", 999900L, 300, 2560, 0, 1, false, true, "[\"5G\",\"旗舰\",\"IOS\"]"},
            {"小米 16 Ultra 徕卡影像旗舰 16GB+1TB", "骁龙8 Gen5 | 徕卡光学 | 5000mAh | 120W秒充", 499900L, 800, 5120, 0, 2, true, true, "[\"5G\",\"徕卡\",\"性价比\"]"},
            {"MacBook Pro 16英寸 M6 Pro芯片 36GB/1TB", "Liquid Retina XDR | 22小时续航 | 深空黑", 1999900L, 200, 1180, 1, 1, true, true, "[\"办公\",\"设计\",\"高性能\"]"},
            {"ThinkPad X1 Carbon Gen 14 2026款", "Ultra 9处理器 | 32GB | 2.8K OLED | 轻至1.08kg", 1499900L, 150, 850, 1, 6, true, false, "[\"商务\",\"轻薄\"]"},
            {"Dell XPS 17 2026 Ultra 9+RTX 5080", "17英寸4K触控屏 | 64GB内存 | 4TB固态", 2499900L, 80, 420, 1, 5, false, true, "[\"设计\",\"高性能\"]"},
            {"Nike Air Jordan 4 Retro 白水泥 复刻版", "经典复刻 | 全掌Air Sole | 耐磨橡胶外底", 149900L, 600, 8900, 3, 8, true, true, "[\"限量\",\"经典\"]"},
            {"Adidas Ultraboost 2026 跑鞋 男女同款", "4D打印中底 | Primeknit+鞋面 | 马牌橡胶底", 99900L, 1000, 6500, 7, 9, true, true, "[\"跑步\",\"舒适\"]"},
            {"欧莱雅 复颜玻尿酸水光充盈导入晶露 50ml", "高浓度玻尿酸 | 深层补水 | 淡化细纹", 29900L, 2000, 12000, 4, 9, false, true, "[\"补水\",\"抗皱\"]"},
            {"茅台 飞天53度 酱香型白酒 500ml 2026年产", "国酒茅台 | 酱香突出 | 幽雅细腻 | 空杯留香", 149900L, 100, 3200, 5, 10, false, true, "[\"送礼\",\"收藏\"]"},
            {"海尔 双开门冰箱 608升 风冷无霜", "一级能效 | 双变频 | 干湿分储 | DEO净味", 499900L, 300, 2100, 2, 11, false, false, "[\"家电\",\"节能\"]"},
            {"格力 云锦-III 大1.5匹 新一级能效 变频冷暖空调", "自清洁 | 独立除湿 | WiFi智控 | 18分贝静音", 399900L, 400, 5600, 2, 12, false, true, "[\"家电\",\"节能\"]"},
            {"美的 智能电饭煲 4L IH电磁加热", "立体加热 | 24h预约 | 不粘内胆 | 多功能菜单", 59900L, 800, 7800, 2, 13, true, false, "[\"家电\",\"智能\"]"},
            {"良品铺子 每日坚果 混合果仁 750g/30袋", "6种坚果+果干 | 科学配比 | 充氮锁鲜", 9900L, 5000, 25000, 5, 14, false, true, "[\"零食\",\"健康\"]"},
            {"三星 Galaxy Tab S10 Ultra 14.6英寸 5G平板", "Dynamic AMOLED 2X | S Pen | IP68防水", 899900L, 250, 980, 0, 3, false, false, "[\"平板\",\"学习\"]"},
            {"索尼 WH-2000XM8 头戴式无线降噪耳机", "全新集成处理器V2 | 40小时续航 | 快充10分钟用5小时", 249900L, 600, 4200, 0, 4, false, true, "[\"耳机\",\"降噪\"]"},
        };

        Random rnd = new Random();
        for (Object[] d : data) {
            Product p = new Product();
            p.setName((String) d[0]);
            p.setDescription((String) d[1]);
            p.setPrice((Long) d[2]);
            p.setStock((Integer) d[3]);
            p.setSales((Integer) d[4]);
            int catIdx = (Integer) d[5];
            int brandIdx = (Integer) d[6];
            if (catIdx < cats.size()) p.setCategoryId(cats.get(catIdx).getId());
            if (brandIdx < brands.size()) p.setBrandId(brands.get(brandIdx).getId());
            p.setIsNew((Boolean) d[7]);
            p.setIsHot((Boolean) d[8]);
            p.setTags((String) d[9]);
            p.setStatus("0");
            productRepo.save(p);

            // Add sample SKUs for some products (first 6)
            if (catIdx <= 1) {
                String[][] specOpts = {
                    {"星空黑", "皓月白", "流光金"},
                    {"128GB", "256GB", "512GB"},
                };
                for (String color : specOpts[0]) {
                    for (String storage : specOpts[1]) {
                        ProductSku sku = new ProductSku();
                        sku.setProductId(p.getId());
                        sku.setName(color + "/" + storage);
                        sku.setPrice(p.getPrice() + (storage.equals("256GB") ? 80000L : storage.equals("512GB") ? 160000L : 0L) + rnd.nextInt(10000));
                        sku.setStock(100 + rnd.nextInt(300));
                        sku.setSkuCode("SKU-" + p.getId() + "-" + color.substring(0,1) + storage);
                        skuRepo.save(sku);
                    }
                }
            }
        }
    }

    private void initBanners() {
        String[][] banners = {
            {"华为 Mate 80 Pro 新品首发", "华为年度旗舰，限时抢购中"},
            {"618 年中大促 爆款直降", "全场低至5折，满199包邮"},
            {"新品首发·小米16 Ultra", "徕卡影像新标杆，预约立减200"},
            {"清凉一夏·空调冰洗节", "空调低至1999，冰箱满千减百"},
        };
        for (int i = 0; i < banners.length; i++) {
            Banner b = new Banner();
            b.setTitle(banners[i][0]);
            b.setImageUrl(""); // placeholder, will use gradient bg
            b.setSortOrder(i);
            if (i == 0) b.setLinkUrl("/product/1");
            else if (i == 2) b.setLinkUrl("/product/3");
            b.setStatus("0");
            bannerRepo.save(b);
        }
    }

    private void initReviews() {
        List<Product> products = productRepo.findAll();
        if (products.size() < 6) return;
        String[][] reviewData = {
            {"user_demo", "5", "非常好用！外观精致，性能强大，拍照效果一流，值得购买。"},
            {"user_demo", "5", "物流很快，包装完好。已经用了几天，运行流畅不卡顿。"},
            {"user_demo", "4", "整体不错，性价比很高。希望系统更新再优化一下续航。"},
            {"user_demo", "5", "颜值很高，手感好。屏幕显示效果清晰，拍照也很出色。"},
            {"user_demo", "4", "质量很好，做工精细。唯一不足是配送比预期晚了一天。"},
            {"user_demo", "5", "第三次购买了，品质一如既往的好。推荐给身边的朋友了。"},
            {"user_demo", "5", "性价比超高！功能齐全，操作流畅，很满意的一次购物。"},
            {"user_demo", "3", "产品还行，但是包装可以再好一些。客服态度很好。"},
        };
        Random rnd = new Random();
        for (String[] rd : reviewData) {
            ProductReview r = new ProductReview();
            r.setProductId(products.get(rnd.nextInt(Math.min(products.size(), 10))).getId());
            r.setUsername(rd[0]);
            r.setRating(Integer.parseInt(rd[1]));
            r.setContent(rd[2]);
            reviewRepo.save(r);
        }
    }

    private void initCoupons() {
        Object[][] coupons = {
            {"满100减10", "full_reduction", 10000L, 1000L},
            {"满300减40", "full_reduction", 30000L, 4000L},
            {"满500减80", "full_reduction", 50000L, 8000L},
            {"满1000减150", "full_reduction", 100000L, 15000L},
            {"9折优惠券（最高减100）", "discount", 0L, 900L},
        };
        for (Object[] c : coupons) {
            Coupon cp = new Coupon();
            cp.setName((String) c[0]);
            cp.setType((String) c[1]);
            cp.setThreshold((Long) c[2]);
            cp.setAmount((Long) c[3]);
            cp.setTotalQty(500);
            cp.setUsedQty(0);
            cp.setStatus("0");
            couponRepo.save(cp);
        }
    }
}
