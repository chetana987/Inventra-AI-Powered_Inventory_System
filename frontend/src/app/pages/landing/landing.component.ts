import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID, HostListener } from '@angular/core';
import { RouterLink } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-landing',
  imports: [RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss'
})
export class LandingComponent implements OnInit, OnDestroy {
  scrolled = false;
  statsVisible = false;
  productsCount = 0;
  transactionsCount = 0;
  accuracyCount = 0;
  private observer: IntersectionObserver | null = null;

  constructor(@Inject(PLATFORM_ID) private platformId: object) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.observer = new IntersectionObserver(
        ([entry]) => {
          if (entry.isIntersecting) {
            this.statsVisible = true;
            this.animateCounters();
            this.observer?.disconnect();
          }
        },
        { threshold: 0.3 }
      );
      const el = document.getElementById('stats-section');
      if (el) this.observer.observe(el);
    }
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    this.scrolled = window.scrollY > 60;
  }

  private animateCounters(): void {
    const targets = [10000, 25000, 99.5];
    const duration = 2000;
    const start = performance.now();

    const step = (now: number) => {
      const t = Math.min((now - start) / duration, 1);
      const ease = 1 - Math.pow(1 - t, 3);
      this.productsCount = Math.floor(ease * targets[0]);
      this.transactionsCount = Math.floor(ease * targets[1]);
      this.accuracyCount = Math.round(ease * targets[2] * 10) / 10;
      if (t < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }
}
